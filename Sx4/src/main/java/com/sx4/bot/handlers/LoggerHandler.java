package com.sx4.bot.handlers;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedAuthor;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedField;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedFooter;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.mongodb.client.model.Projections;
import com.sx4.bot.config.Config;
import com.sx4.bot.core.Sx4;
import com.sx4.bot.database.Database;
import com.sx4.bot.entities.management.logger.LoggerCategory;
import com.sx4.bot.entities.management.logger.LoggerEvent;
import com.sx4.bot.managers.LoggerManager;
import com.sx4.bot.utility.StringUtility;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LoggerHandler extends ListenerAdapter {

	public class LoggerContext {

		private final User user;
		private final GuildChannel channel;
		private final Role role;

		public LoggerContext(User user, GuildChannel channel, Role role) {
			this.user = user;
			this.channel = channel;
			this.role = role;
		}

		public User getUser() {
			return this.user;
		}

		public GuildChannel getChannel() {
			return this.channel;
		}

		public Role getRole() {
			return this.role;
		}

	}

	private final LoggerManager manager = LoggerManager.get();
	private final Database database = Database.get();
	private final Config config = Config.get();

	private boolean canSend(Document logger, LoggerEvent event, LoggerContext context) {
		if (!logger.get("enabled", true)) {
			return false;
		}

		if ((logger.get("events", LoggerEvent.ALL) & event.getRaw()) != event.getRaw()) {
			return false;
		}

		List<Document> entities = logger.getEmbedded(List.of("blacklist", "entities"), Collections.emptyList());
		for (Document entity : entities) {
			if ((entity.getLong("events") & event.getRaw()) != event.getRaw()) {
				continue;
			}

			long id = entity.getLong("id");
			LoggerCategory category = LoggerCategory.fromType(entity.getInteger("type"));
			switch (category) {
				case ROLE:
					if (context.getRole().getIdLong() == id) {
						return false;
					}

					break;
				case USER:
					if (context.getUser().getIdLong() == id) {
						return false;
					}

					break;
				case VOICE_CHANNEL:
				case TEXT_CHANNEL:
				case STORE_CHANNEL:
					if (context.getChannel().getIdLong() == id) {
						return false;
					}

					break;
				case CATEGORY:
					GuildChannel channel = context.getChannel();
					GuildChannel parent = channel.getParent();
					if ((parent == null && channel.getIdLong() == id) || (parent != null && parent.getIdLong() == id)) {
						return false;
					}

					break;
			}
		}

		return true;
	}

	private void onMessageDelete(TextChannel textChannel, List<Long> messageIds) {
		Guild guild = textChannel.getGuild();
		ShardManager shardManager = Sx4.get().getShardManager();

		LoggerEvent loggerEvent = LoggerEvent.MESSAGE_DELETE;

		List<Document> loggers = this.database.getGuildById(guild.getIdLong(), Projections.include("logger.loggers")).getEmbedded(List.of("logger", "loggers"), Collections.emptyList());
		for (Document logger : loggers) {
			if (!logger.get("enabled", true)) {
				continue;
			}

			if ((logger.get("events", LoggerEvent.ALL) & loggerEvent.getRaw()) != loggerEvent.getRaw()) {
				continue;
			}

			TextChannel channel = guild.getTextChannelById(logger.getLong("id"));
			if (channel == null) {
				//delete data
			}

			List<WebhookEmbed> embeds = new ArrayList<>();
			for (long messageId : messageIds) {
				WebhookEmbedBuilder embed = new WebhookEmbedBuilder()
					.setColor(this.config.getRed())
					.setTimestamp(Instant.now())
					.setFooter(new EmbedFooter(String.format("Message ID: %d", messageId), null));

				Document message = this.database.getMessageById(messageId);
				if (message == null) {
					embed.setDescription(String.format("A message sent in %s was deleted", textChannel.getAsMention()));
					embed.setAuthor(new EmbedAuthor(guild.getName(), guild.getIconUrl(), null));
				} else {
					// TODO: check blacklists

					long userId = message.getLong("authorId");
					User user = shardManager.getUserById(userId);

					embed.setDescription(String.format("The message sent by `%s` in %s was deleted", user == null ? userId : user.getName(), textChannel.getAsMention()));
					embed.setAuthor(new EmbedAuthor(user == null ? guild.getName() : user.getAsTag(), user == null ? guild.getIconUrl() : user.getEffectiveAvatarUrl(), null));

					String content = message.getString("content");
					if (content.length() != 0) {
						embed.addField(new EmbedField(false, "Message", StringUtility.limit(content, MessageEmbed.VALUE_MAX_LENGTH, "...")));
					}
				}

				embeds.add(embed.build());
			}

			this.manager.queue(channel, logger, embeds);
		}
	}

	public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
		this.onMessageDelete(event.getChannel(), List.of(event.getMessageIdLong()));
	}

	public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
		this.onMessageDelete(event.getChannel(), event.getMessageIds().stream().map(Long::parseLong).collect(Collectors.toList()));
	}

	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		VoiceChannel channel = event.getChannelJoined();

		LoggerEvent loggerEvent = LoggerEvent.MEMBER_VOICE_JOIN;
		LoggerContext loggerContext = new LoggerContext(member.getUser(), channel, null);

		List<Document> loggers = this.database.getGuildById(guild.getIdLong(), Projections.include("logger.loggers")).getEmbedded(List.of("logger", "loggers"), Collections.emptyList());
		for (Document logger : loggers) {
			if (!this.canSend(logger, loggerEvent, loggerContext)) {
				return;
			}

			TextChannel textChannel = guild.getTextChannelById(logger.getLong("id"));
			if (textChannel == null) {
				//delete data
			}

			WebhookEmbedBuilder embed = new WebhookEmbedBuilder()
				.setDescription(String.format("`%s` just joined the voice channel `%s`", member.getEffectiveName(), channel.getName()))
				.setColor(this.config.getGreen())
				.setTimestamp(Instant.now())
				.setFooter(new EmbedFooter(String.format("User ID: %s", member.getId()), null))
				.setAuthor(new EmbedAuthor(member.getUser().getAsTag(), member.getUser().getEffectiveAvatarUrl(), null));

			this.manager.queue(textChannel, logger, embed.build());
		}
	}

}
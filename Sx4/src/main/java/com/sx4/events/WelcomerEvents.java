package com.sx4.events;

import static com.rethinkdb.RethinkDB.r;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import com.sx4.core.Sx4Bot;
import com.sx4.utils.WelcomerUtils;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.tuple.Pair;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookMessage;
import net.dv8tion.jda.webhook.WebhookMessageBuilder;
import okhttp3.OkHttpClient;

public class WelcomerEvents extends ListenerAdapter {
	
	private OkHttpClient client = new OkHttpClient.Builder().build();
	
	private ScheduledExecutorService scheduledExectuor = Executors.newSingleThreadScheduledExecutor();
	
	private Map<String, Pair<Webhook, WebhookClient>> welcomerWebhooks = new HashMap<>();
	private Map<String, Pair<Webhook, WebhookClient>> leaverWebhooks = new HashMap<>();
	
	private void getWelcomerWebhook(Guild guild, Map<String, Object> data, Consumer<WebhookClient> webhook) throws MalformedURLException, IOException {
		TextChannel channel = guild.getTextChannelById((String) data.get("channel"));
		if (channel == null) {
			return;
		}
		
		User selfUser = guild.getSelfMember().getUser();
		if (this.welcomerWebhooks.containsKey(guild.getId())) {
			Webhook currentWebhook = this.welcomerWebhooks.get(guild.getId()).getLeft();
			if (!currentWebhook.getChannel().equals(channel)) {
				currentWebhook.delete().queue();
				channel.createWebhook(selfUser.getName() + " - Welcomer").setAvatar(Icon.from(new URL(selfUser.getEffectiveAvatarUrl()).openStream())).queue(newWebhook -> {
					WebhookClient webhookClient = newWebhook.newClient()
							.setHttpClient(this.client)
							.setExecutorService(this.scheduledExectuor)
							.build();
					this.welcomerWebhooks.put(guild.getId(), Pair.of(newWebhook, webhookClient));
					webhook.accept(webhookClient);
				});
			} else {
				WebhookClient webhookClient = this.welcomerWebhooks.get(guild.getId()).getRight();
				webhook.accept(webhookClient);
			}
		} else {
			channel.getWebhooks().queue(webhooks -> {
				for (Webhook channelWebhook : webhooks) {
					if (channelWebhook.getName().equals(selfUser.getName() + " - Welcomer")) {
						if (!channelWebhook.getChannel().equals(channel)) {
							channelWebhook.delete().queue();
							try {
								channel.createWebhook(selfUser.getName() + " - Welcomer").setAvatar(Icon.from(new URL(selfUser.getEffectiveAvatarUrl()).openStream())).queue(newWebhook -> {
									WebhookClient webhookClient = newWebhook.newClient()
											.setHttpClient(this.client)
											.setExecutorService(this.scheduledExectuor)
											.build();
									this.welcomerWebhooks.put(guild.getId(), Pair.of(newWebhook, webhookClient));
									webhook.accept(webhookClient);
								});
							} catch (IOException e) {}
						} else {
							WebhookClient webhookClient = channelWebhook.newClient()
									.setHttpClient(this.client)
									.setExecutorService(this.scheduledExectuor)
									.build();
							this.welcomerWebhooks.put(guild.getId(), Pair.of(channelWebhook, webhookClient));
							webhook.accept(webhookClient);
						}
						
						return;
					}
				}
				
				try {
					channel.createWebhook(selfUser.getName() + " - Welcomer").setAvatar(Icon.from(new URL(selfUser.getEffectiveAvatarUrl()).openStream())).queue(newWebhook -> {
						WebhookClient webhookClient = newWebhook.newClient()
								.setHttpClient(this.client)
								.setExecutorService(this.scheduledExectuor)
								.build();
						this.welcomerWebhooks.put(guild.getId(), Pair.of(newWebhook, webhookClient));
						webhook.accept(webhookClient);
					});
				} catch (IOException e) {}
			});
		}
	}
	
	private void getLeaverWebhook(Guild guild, Map<String, Object> data, Consumer<WebhookClient> webhook) throws MalformedURLException, IOException {
		TextChannel channel = guild.getTextChannelById((String) data.get("leavechannel"));
		if (channel == null) {
			return;
		}
		
		User selfUser = guild.getSelfMember().getUser();
		if (this.leaverWebhooks.containsKey(guild.getId())) {
			Webhook currentWebhook = this.leaverWebhooks.get(guild.getId()).getLeft();
			if (!currentWebhook.getChannel().equals(channel)) {
				currentWebhook.delete().queue();
				channel.createWebhook(selfUser.getName() + " - Leaver").setAvatar(Icon.from(new URL(selfUser.getEffectiveAvatarUrl()).openStream())).queue(newWebhook -> {
					WebhookClient webhookClient = newWebhook.newClient()
							.setHttpClient(this.client)
							.setExecutorService(this.scheduledExectuor)
							.build();
					this.leaverWebhooks.put(guild.getId(), Pair.of(newWebhook, webhookClient));
					webhook.accept(webhookClient);
				});
			} else {
				WebhookClient webhookClient = this.leaverWebhooks.get(guild.getId()).getRight();
				webhook.accept(webhookClient);
			}
		} else {
			channel.getWebhooks().queue(webhooks -> {
				for (Webhook channelWebhook : webhooks) {
					if (channelWebhook.getName().equals(selfUser.getName() + " - Leaver")) {
						if (!channelWebhook.getChannel().equals(channel)) {
							channelWebhook.delete().queue();
							try {
								channel.createWebhook(selfUser.getName() + " - Leaver").setAvatar(Icon.from(new URL(selfUser.getEffectiveAvatarUrl()).openStream())).queue(newWebhook -> {
									WebhookClient webhookClient = newWebhook.newClient()
											.setHttpClient(this.client)
											.setExecutorService(this.scheduledExectuor)
											.build();
									this.leaverWebhooks.put(guild.getId(), Pair.of(newWebhook, webhookClient));
									webhook.accept(webhookClient);
								});
							} catch (IOException e) {}
						} else {
							WebhookClient webhookClient = channelWebhook.newClient()
									.setHttpClient(this.client)
									.setExecutorService(this.scheduledExectuor)
									.build();
							this.leaverWebhooks.put(guild.getId(), Pair.of(channelWebhook, webhookClient));
							webhook.accept(webhookClient);
						}
						
						return;
					}
				}
				
				try {
					channel.createWebhook(selfUser.getName() + " - Leaver").setAvatar(Icon.from(new URL(selfUser.getEffectiveAvatarUrl()).openStream())).queue(newWebhook -> {
						WebhookClient webhookClient = newWebhook.newClient()
								.setHttpClient(this.client)
								.setExecutorService(this.scheduledExectuor)
								.build();
						this.leaverWebhooks.put(guild.getId(), Pair.of(newWebhook, webhookClient));
						webhook.accept(webhookClient);
					});
				} catch (IOException e) {}
			});
		}
	}

	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Map<String, Object> data = r.table("welcomer").get(event.getGuild().getId()).run(Sx4Bot.getConnection());
		if (data == null || ((boolean) data.get("toggle") == false && (boolean) data.get("imgwelcomertog") == false) || data.get("channel") == null) {
			return;
		}
		
		try {
			this.getWelcomerWebhook(event.getGuild(), data, webhook -> {
				WelcomerUtils.getWelcomerMessage(event.getMember(), event.getGuild(), data, (message, response) -> {
					if (message.isEmpty() && response != null) {
						byte[] bytes;
						try {
							bytes = response.body().bytes();
						} catch (IOException e) {
							return;
						}
						
						try {
							webhook.send(bytes, "welcomer." + response.headers().get("Content-Type").split("/")[1]);
						} catch (IllegalArgumentException e) {}
					} else if (!message.isEmpty() && response == null) {
						webhook.send(message.build());
					} else {
						byte[] bytes;
						try {
							bytes = response.body().bytes();
						} catch (IOException e) {
							return;
						}
						
						try {
							WebhookMessage webhookMessage = new WebhookMessageBuilder(message.build()).addFile("welcomer." + response.headers().get("Content-Type").split("/")[1], bytes).build();
							webhook.send(webhookMessage);
						} catch (IllegalArgumentException e) {}
					}
				});
			});
		} catch (IOException e) {}
	}
	
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		Map<String, Object> data = r.table("welcomer").get(event.getGuild().getId()).run(Sx4Bot.getConnection());
		if (data == null || (boolean) data.get("leavetoggle") == false || data.get("leavechannel") == null) {
			return;
		}

		try {
			this.getLeaverWebhook(event.getGuild(), data, webhook -> {
				webhook.send(WelcomerUtils.getLeaver(event.getMember(), event.getGuild(), data).build());
			});
		} catch (IOException e) {}
	}
}

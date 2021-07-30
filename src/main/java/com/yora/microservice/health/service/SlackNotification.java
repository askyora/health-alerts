package com.yora.microservice.health.service;

import static com.slack.api.model.block.Blocks.asBlocks;
import static com.slack.api.model.block.Blocks.section;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.composition.TextObject;
import com.yora.microservice.health.dto.ServiceHealthUrl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnExpression("${notification.slack.enabled:true}")
public class SlackNotification implements Notification {

  private static final String UP = "UP";

  private static final String DOWN = "DOWN";

  @Value("${notification.slack.token}")
  private String token = "";

  @Value("${notification.slack.channel:#production_health}")
  private String channel = "#production_health";

  @Value("${notification.slack.server-name:Production Server}")
  private String serverName = "Production}";

  private Slack slack = null;

  @PostConstruct
  public void init() {
    slack = Slack.getInstance();
  }

  @Override
  public void publish(List<ServiceHealthUrl> newList, List<ServiceHealthUrl> previous) {

    try {

      MethodsClient methodsClient = slack.methods(token);

      String overallStatus =
          newList.stream().filter(e -> !e.getStatus().equalsIgnoreCase(UP)).findFirst().isPresent()
              ? DOWN
              : UP;

      StringBuilder sb = new StringBuilder();

      sb.append("*Overall Server [")
          .append(serverName)
          .append("] Status* : *")
          .append(overallStatus)
          .append("*")
          .append("\n");

      Optional<ZonedDateTime> downFromNew =
          newList.stream()
              .filter(e -> !e.getStatus().equalsIgnoreCase(UP))
              .map(e -> e.getTimestamp())
              .min((x, y) -> x.compareTo(y));

      if (previous != null) {

        Optional<ZonedDateTime> downFromPrevious =
            previous.stream()
                .filter(e -> !e.getStatus().equalsIgnoreCase(UP))
                .map(e -> e.getTimestamp())
                .min((x, y) -> x.compareTo(y));
        if (downFromPrevious.isPresent())
          sb.append("*Sevice Down Since* : *").append(downFromPrevious.get()).append("*");

      } else {

        if (downFromNew.isPresent()) {
          sb.append("*Sevice Down Since* : *").append(downFromNew.get()).append("*");
        }
      }

      methodsClient.chatPostMessage(
          req ->
              req.channel(channel)
                  .blocks(
                      asBlocks(
                          section(
                              section ->
                                  section.text(
                                      MarkdownTextObject.builder().text(sb.toString()).build())))));

      String services = newList.stream().map(e -> e.getName()).collect(Collectors.joining("\n"));

      String status = newList.stream().map(e -> e.getStatus()).collect(Collectors.joining("\n"));

      List<TextObject> list = new ArrayList<>();

      list.add(MarkdownTextObject.builder().text("*Service Name*").build());
      list.add(MarkdownTextObject.builder().text("*Status*").build());

      list.add(PlainTextObject.builder().text(services).build());
      list.add(PlainTextObject.builder().text(status).build());

      ChatPostMessageResponse response =
          methodsClient.chatPostMessage(
              req ->
                  req.channel(channel).blocks(asBlocks(section(section -> section.fields(list)))));

      log.warn(response.toString());
    } catch (IOException | SlackApiException e) {
      log.error(e.getMessage(), e);
    }
  }
}

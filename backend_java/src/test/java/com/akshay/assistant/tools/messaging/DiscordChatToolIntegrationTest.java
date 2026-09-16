// package com.akshay.assistant.tools.messaging;

// import com.akshay.assistant.tools.messaging.entity.AvailableChatEntity;
// import com.akshay.assistant.tools.messaging.model.ChatResponse;
// import com.akshay.assistant.tools.messaging.model.ConversationHistoryResponse;
// import com.akshay.assistant.tools.messaging.model.UnreadChatsResponse;
// import com.akshay.assistant.tools.messaging.repository.AvailableChatRepository;

// import org.junit.jupiter.api.*;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import java.time.Instant;

// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest() 
// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
// class DiscordChatToolIntegrationTest {

//     @Autowired
//     private DiscordChatTool discordChatTool;
//     @Autowired 
//     private AvailableChatRepository chatRepository;

//     @BeforeEach
//     void setUp() {
//         AvailableChatEntity chat = new AvailableChatEntity();

//         chat.setAuthorId("856137411649208360");
//         chat.setChannelId("1549352746912710786");
//         chat.setName("ak7150");
//         chat.setGlobalName("Akshay");

//         chatRepository.save(chat);
//     }
//     @BeforeAll
//     void waitForDiscordBot() throws InterruptedException {
//         /*
//          * JDA connects asynchronously.
//          *
//          * Give the bot enough time to establish the Discord Gateway
//          * connection before executing integration tests.
//          */
//         Thread.sleep(5_000);
//     }

//     @Test
//     void shouldListUnreadChats() {

//         UnreadChatsResponse result =
//                 discordChatTool.listUnreadChats();

//         assertNotNull(result);
//         assertEquals("success", result.status());

//         assertNotNull(result.chatList());
//         assertEquals(
//                 result.count(),
//                 result.chatList().size()
//         );
//     }

//     @Test
//     void shouldOpenConversation() {

//         String chatName = "general";

//         ChatResponse result =
//                 discordChatTool.openConversation(chatName);

//         assertNotNull(result);

//         if ("error".equals(result.status())) {
//             assertNotEquals(
//                     "ChatNotFound",
//                     result.error(),
//                     "Test Discord user/channel was not found"
//             );
//         } else {
//             assertEquals("success", result.status());
//         }
//     }

//     @Test
//     void shouldSendMessageToDiscord() {

//         String chatName = "ak7150";

//         ChatResponse result =
//                 discordChatTool.sendMessage(
//                         "Discord integration test - "
//                                 + Instant.now(),
//                         chatName
//                 );

//         assertNotNull(result);
//         assertEquals(
//                 "success",
//                 result.status(),
//                 result.message()
//         );
//     }

//     @Test
//     void shouldSendMessageUsingOpenConversation() {

//         String chatName = "general";

//         ChatResponse openResult =
//                 discordChatTool.openConversation(chatName);

//         assertEquals(
//                 "success",
//                 openResult.status(),
//                 openResult.message()
//         );

//         ChatResponse sendResult =
//                 discordChatTool.sendMessage(
//                         "Message sent using active conversation - "
//                                 + Instant.now(),
//                         null
//                 );

//         assertNotNull(sendResult);

//         assertEquals(
//                 "success",
//                 sendResult.status(),
//                 sendResult.message()
//         );
//     }

//     @Test
//     void shouldGetConversationHistory() {

//         String chatName = "general";

//         ConversationHistoryResponse result =
//                 discordChatTool.getConversationHistory(
//                         chatName,
//                         20,
//                         null,
//                         null
//                 );

//         assertNotNull(result);
//         assertEquals(
//                 "success",
//                 result.status(),
//                 result.message()
//         );

//         assertNotNull(result.messageList());

//         assertEquals(
//                 result.count(),
//                 result.messageList().size()
//         );
//     }

//     @Test
//     void shouldCloseConversation() {

//         /*
//          * Opening first makes this test independent from
//          * test execution order.
//          */
//         String chatName = "general";

//         ChatResponse openResult =
//                 discordChatTool.openConversation(chatName);

//         assertEquals(
//                 "success",
//                 openResult.status()
//         );

//         ChatResponse closeResult =
//                 discordChatTool.closeConversation();

//         assertNotNull(closeResult);

//         assertEquals(
//                 "success",
//                 closeResult.status()
//         );
//     }

//     @Test
//     void shouldFailWhenSendingWithoutOpenConversation() {

//         /*
//          * Make sure this test does not inherit state from another test.
//          */
//         discordChatTool.closeConversation();

//         ChatResponse result =
//                 discordChatTool.sendMessage(
//                         "This should fail",
//                         null
//                 );

//         assertNotNull(result);

//         assertEquals(
//                 "error",
//                 result.status()
//         );

//         assertEquals(
//                 "NoOpenConversation",
//                 result.error()
//         );
//     }

//     @Test
//     void shouldFailForUnknownChat() {

//         ChatResponse result =
//                 discordChatTool.openConversation(
//                         "definitely-not-a-real-discord-chat-"
//                                 + System.currentTimeMillis()
//                 );

//         assertNotNull(result);

//         assertEquals(
//                 "error",
//                 result.status()
//         );

//         assertEquals(
//                 "ChatNotFound",
//                 result.error()
//         );
//     }
// }
package tests;

import arc.*;
import arc.func.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mmc.utils.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.util.stream.*;
import java.util.stream.Stream.*;

public class ToolsTests{
    @BeforeAll
    static void beforeAll(){
        System.out.println("Tools tests begin");
    }

    @AfterAll

    static void afterAll(){
        System.out.println("Tools tests end");
    }

    private static Stream<Arguments> randomInts(){
        class IntArguments implements Arguments{
            @Override
            public Object[] get(){
                return new Integer[]{Mathf.rand.nextInt(), Mathf.rand.nextInt(), Mathf.rand.nextInt()};
//                return new Integer[]{1,2,3};
            }
        }
        Builder<Arguments> builder = Stream.builder();
        for(int i = 0; i < 10; i++){
            builder.add(new IntArguments());
        }
        return builder.build();
    }

    @BeforeEach
    void beforeEach(){
        Mathf.rand.setSeed(0);
        ObjectMap<Object, Seq<Cons<?>>> events = Reflect.get(Events.class, "events");
        events.clear();
    }

    @ParameterizedTest
//    @ValueSource(ints = {1,2,3,4,4214214,656,467,54,123,21,321,551421,4,1,4214,21,4,6316,1537,548,54234367,2563,56})
    @MethodSource("randomInts")
    void eventOperations(int a, int b, int c){
        EventSender sender = new EventSender("test-event");
        EventReceiver receiver = new EventReceiver("test-event");
        boolean[] receiverInvoked={false};
        boolean[] senderCallbackInvoked={false};
        receiver.post(e -> {
            Assertions.assertEquals(4, e.paramsAmount());
            e.<Intc>getParameter("listener").get(
            e.getNumParam("a").intValue() +
            e.getNumParam("b").intValue() +
            e.getNumParam("c").intValue()
            );
            receiverInvoked[0]=true;
        });

        sender.setParameter("a", a);
        sender.setParameter("b", b);
        sender.setParameter("c", c);
        sender.<Intc>setParameter("listener", i -> {
           Assertions.assertEquals(a + b + c, i);
            senderCallbackInvoked[0]=true;
        });
        sender.fire();

        Assertions.assertTrue(receiverInvoked[0],"receiver not invoked");
        Assertions.assertTrue(senderCallbackInvoked[0],"sender callback not invoked");
    }

    @Test
    void receiverParamsClearTests(){
        EventSender sender = new EventSender("test-event");
        EventReceiver receiver = new EventReceiver("test-event");

        receiver.post(e -> {
        });
        sender.fire();
        ObjectMap<String, Object> parametersMap = Reflect.get(EventReceiver.class, receiver, "parametersMap");
        Assertions.assertEquals(0,parametersMap.size,"Receiver params should be clear after post block");
    }
    @Test
    void senderParamsClearTests(){
        EventSender sender = new EventSender("test-event");
        EventReceiver receiver = new EventReceiver("test-event");
        ObjectMap<String, Object> parametersMap;
        receiver.post(e -> {
        });
        sender.setParameter("Hello world","Hello world");
        sender.fire();
        parametersMap = Reflect.get(EventSender.class, sender, "parametersMap");
        Assertions.assertEquals(1,parametersMap.size,"Sender params should be saved after fire");

        sender.fire(true);
        parametersMap = Reflect.get(EventSender.class, sender, "parametersMap");
        Assertions.assertEquals(0,parametersMap.size,"Sender params should be clear after fire(true)");

        sender.setParameter("Hello world","Hello world");
        sender.fire(false);
        parametersMap = Reflect.get(EventSender.class, sender, "parametersMap");
        Assertions.assertEquals(1,parametersMap.size,"Sender params should be saved after fire(false)");
    }
}

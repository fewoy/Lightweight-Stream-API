package com.annimon.stream;

import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.Predicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author aNNiMON
 */
public class StreamTest {
    
    private static final Integer[] array10 = new Integer[10];
    private static final List<Integer> list10 = new LinkedList<Integer>();
    private static final List<Integer> listRandom10 = new LinkedList<Integer>();
    private PrintConsumer pc1, pc2;
    
    @BeforeClass
    public static void setUpData() {
        final Random rnd = new Random();
        for (int i = 0; i < 10; i++) {
            array10[i] = i;
            list10.add(i);
            listRandom10.add(rnd.nextInt(10));
        }
    }
    
    @Before
    public void setUp() {
         pc1 = new PrintConsumer();
         pc2 = new PrintConsumer();
    }
    
    @Test
    public void limit() {
        Stream.of(array10).limit(2).forEach(pc1.getConsumer());
        assertEquals("01", pc1.toString());
        
        Stream.of(list10).limit(2).forEach(pc2.getConsumer());
        assertEquals(pc1.out(), pc2.out());
    }
    
    @Test
    public void skip() {
        Stream.of(array10).skip(7).forEach(pc1.getConsumer());
        assertEquals("789", pc1.toString());
        
        Stream.of(list10).skip(7).forEach(pc2.getConsumer());
        assertEquals(pc1.out(), pc2.out());
    }
    
    @Test
    public void limitAndSkip() {
        Stream.of(array10).skip(2).limit(5).forEach(pc1.getConsumer());
        assertEquals("23456", pc1.out());
        
        Stream.of(list10).limit(5).skip(2).forEach(pc1.getConsumer());
        assertEquals("234", pc1.out());
        
        Stream.of(list10).skip(8).limit(15).forEach(pc1.getConsumer());
        assertEquals("89", pc1.out());
    }
    
    @Test
    public void filter() {
        Stream.of(array10).filter(pc1.getFilter(2)).forEach(pc1.getConsumer());
        assertEquals("02468", pc1.toString());
        
        Stream.of(list10).filter(pc2.getFilter(2)).forEach(pc2.getConsumer());
        assertEquals("02468", pc1.toString());
        
        assertEquals(pc1.out(), pc2.out());
        
        Stream.of(array10).filter(Predicate.Util.or(pc1.getFilter(2), pc1.getFilter(3))).forEach(pc1.getConsumer());
        assertEquals("0234689", pc1.out());
        
        Stream.of(list10).filter(Predicate.Util.and(pc1.getFilter(2), pc1.getFilter(3))).forEach(pc1.getConsumer());
        assertEquals("06", pc1.out());
        
        Stream.of(list10).filter(pc1.getFilter(2)).filter(pc1.getFilter(3)).forEach(pc1.getConsumer());
        assertEquals("06", pc1.out());
    }
    
    @Test
    public void map() {
        final Function<Number, String> mapToString = new Function<Number, String>() {
            @Override
            public String apply(Number t) {
                return String.format("[%d]", (int) Math.sqrt(t.intValue()));
            }
        };
        final Function<String, Integer> mapToInt = new Function<String, Integer>() {
            @Override
            public Integer apply(String t) {
                final String str = t.substring(1, t.length() - 1);
                final int value = Integer.parseInt(str);
                return value * value;
            }
        };
        Stream.of(4, 9, 16).map(mapToString).forEach(pc1.getConsumer());
        assertEquals("[2][3][4]", pc1.out());
        
        Stream s1 = Stream.of(25, 64, 625);
        Stream s2 = Stream.of("[5]", "[8]", "[25]");
        s2.forEach(pc2.getConsumer());
        s1.map(mapToString).forEach(pc1.getConsumer());
        assertEquals(pc1.out(), pc2.out());
        s2.map(mapToInt).forEach(pc2.getConsumer());
        s1.forEach(pc1.getConsumer());
        assertEquals(pc1.out(), pc2.out());
        
        final Function<Integer, Integer> mapPlus1 = new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer x) {
                return x + 1;
            }
        };
        final Function<Integer, Integer> mapPlus2 = Function.Util.andThen(mapPlus1, mapPlus1);
        Stream.ofRange(-10, 0)
                .map(mapPlus2)
                .map(mapPlus2)
                .map(mapPlus2)
                .map(mapPlus2)
                .map(mapPlus2)
                .forEach(pc1.getConsumer());
        assertEquals("0123456789", pc1.out());
    }
    
    @Test
    public void flatMap() {
        Stream.ofRange(2, 4)
                .flatMap(new Function<Integer, Stream<String>>() {

                    @Override
                    public Stream<String> apply(final Integer i) {
                        return Stream.ofRange(2, 4).map(new Function<Integer, String>() {

                            @Override
                            public String apply(Integer p) {
                                return String.format("%d*%d=%d\n", i, p, (i*p));
                            }
                        });
                    }
                })
                .forEach(pc1.getConsumer());
        assertEquals("2*2=4\n2*3=6\n3*2=6\n3*3=9\n", pc1.out());
    }
    
    @Test
    public void sorted() {
        Stream.of(6, 3, 9, 0, -7, 19).sorted().forEach(pc1.getConsumer());
        assertEquals("-7036919", pc1.out());
        
        Stream.of(array10).sorted().forEach(pc1.getConsumer());
        Stream.of(list10).sorted().forEach(pc2.getConsumer());
        assertEquals(pc1.out(), pc2.out());
        
        Stream.of(listRandom10).sorted().forEach(pc1.getConsumer());
        List<Integer> list = new ArrayList<Integer>(listRandom10);
        Collections.sort(list);
        Stream.of(list).forEach(pc2.getConsumer());
        assertEquals(pc1.out(), pc2.out());
    }
    
    @Test
    public void count() {
        long count = Stream.ofRange(10000000000L, 10000010000L).count();
        assertEquals(10000, count);
        
        long count1 = Stream.of(array10).peek(pc1.getConsumer()).count();
        long count2 = Stream.of(list10).peek(pc2.getConsumer()).count();
        assertEquals(count1, count2);
        assertEquals(pc1.out(), pc2.out());
    }
    
    @Test
    public void match() {
        boolean match;
        
        match = Stream.of(array10).anyMatch(pc1.getFilter(2));
        assertEquals(true, match);
        match = Stream.of(2, 3, 5, 8, 13).anyMatch(pc1.getFilter(10));
        assertEquals(false, match);
        
        match = Stream.of(array10).allMatch(pc1.getFilter(2));
        assertEquals(false, match);
        match = Stream.of(2, 4, 6, 8, 10).allMatch(pc1.getFilter(2));
        assertEquals(true, match);
        
        match = Stream.of(array10).noneMatch(pc1.getFilter(2));
        assertEquals(false, match);
        match = Stream.of(2, 3, 5, 8, 13).noneMatch(pc1.getFilter(10));
        assertEquals(true, match);
    }
    
    private class PrintConsumer {
        
        private final StringBuilder out = new StringBuilder();

        public <T> Consumer<T> getConsumer() {
            return new Consumer<T>() {

                @Override
                public void accept(T value) {
                    out.append(value);
                }
            };
        }
        
        private Predicate<Integer> getFilter(final int val) {
            return new Predicate<Integer>() {

                @Override
                public boolean test(Integer v) {
                    return (v % val == 0);
                }
            };
        }
        
        public String out() {
            String result = out.toString();
            out.setLength(0);
            return result;
        }

        @Override
        public String toString() {
            return out.toString();
        }
    }
}

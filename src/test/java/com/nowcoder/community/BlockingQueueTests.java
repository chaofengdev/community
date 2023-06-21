package com.nowcoder.community;


import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueTests {

    public static void main(String[] args) {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);//阻塞队列
        //生产线程
        Thread thread_producer = new Thread(new Producer(queue));
        thread_producer.start();
        //消费线程1
        Thread thread_consumer_1 = new Thread(new Consumer(queue));
        thread_consumer_1.start();
        //消费线程2
        Thread thread_consumer_2 = new Thread(new Consumer(queue));
        thread_consumer_2.start();
        //消费线程3
        Thread thread_consumer_3 = new Thread(new Consumer(queue));
        thread_consumer_3.start();
    }
}

/**
 * ==========================================================================================
 * Runnable接口
 * Runnable接口是Java中定义的一个函数式接口（Functional Interface），用于表示可以由线程执行的任务。它是多线程编程的基础之一，用于创建可并发执行的代码块。
 *
 * Runnable接口只包含一个抽象方法run()，该方法没有参数和返回值。当一个类实现了Runnable接口并提供了run()方法的实现，该类的实例可以被当作线程执行的任务，
 * 可以通过创建线程对象并将其作为参数传递给Thread类的构造方法来执行。
 *
 * 使用Runnable接口的主要步骤如下：
 *
 * 创建一个实现了Runnable接口的类，并实现run()方法，该方法包含需要在新线程中执行的代码逻辑。
 * 创建一个Thread对象，将上述实现了Runnable接口的类的实例作为参数传递给Thread的构造方法。
 * 调用Thread对象的start()方法，启动新线程，并自动调用传递的Runnable对象的run()方法。
 *
 * 通过实现Runnable接口，可以将需要并发执行的逻辑封装为可重用的任务，并且与线程的生命周期分离，提高了代码的灵活性和可维护性。
 * ==========================================================================================
 * 函数式接口（Functional Interface）
 * 是Java 8引入的一个概念，它是指仅包含一个抽象方法的接口。函数式接口在函数式编程中扮演重要的角色，它可以被视为一个可以用作函数的对象。
 *
 * Java中的函数式接口具有以下特点：
 *
 * 只包含一个抽象方法：函数式接口只能包含一个未实现的抽象方法，用于表示函数式接口的功能或行为。
 * 可以有默认方法：函数式接口可以定义默认方法，这些方法可以有默认的实现，供接口的实现类使用。
 * 可以有静态方法：函数式接口可以定义静态方法，这些方法与接口的实例无关，可以直接通过接口名称调用。
 * 函数式接口的引入主要是为了支持Lambda表达式和函数式编程的特性。
 * Lambda表达式可以将一个函数式接口的实例作为参数传递，或者作为返回值返回。通过Lambda表达式，可以以更简洁和灵活的方式编写函数式代码。
 *
 * 函数式接口提供了一种更简洁、更灵活的编程方式，可以将行为作为参数传递，使得代码更易读、易于维护和扩展。
 * 此外，Java标准库中也提供了一些常用的函数式接口，如Predicate、Consumer、Supplier和Function等，用于支持函数式编程的各种场景。
 * ==========================================================================================
 * Lambda表达式是用来简化匿名内部类的吗？
 * Lambda表达式的一个主要目的是简化使用匿名内部类的代码。
 * 在以前的Java版本中，如果想要创建一个接口的实例，并实现其抽象方法，通常需要使用匿名内部类的语法，这会导致代码显得冗长而繁琐。
 *
 * Lambda表达式的引入旨在提供一种更简洁、更直观的语法来创建函数式接口的实例。
 * Lambda表达式可以通过一种更紧凑的语法形式来定义和实现接口的抽象方法，从而避免了显式地编写匿名内部类的代码。
 *
 * 使用Lambda表达式可以更直接地表达函数式接口的功能，并将关注点放在要执行的操作上，而不是围绕创建接口实例的语法细节。
 * 它大大简化了函数式编程和事件驱动编程中的回调和处理逻辑的编写。
 * 通过使用Lambda表达式，可以减少冗长的语法和模板代码，使代码更加清晰、易读和易于维护。
 * 它是Java 8引入的一项重要特性，对于函数式编程和事件驱动编程非常有用。
 * ==========================================================================================
 */
class Producer implements Runnable{

    /**
     * ==========================================================================================
     * BlockingQueue是Java中的一个接口，它继承自java.util.Queue接口，并在其基础上提供了一些额外的阻塞操作。
     * 它是用于在多线程环境下进行线程安全和同步的队列实现。
     *
     * BlockingQueue接口定义了几种阻塞队列的常见方法，包括：
     *
     * put(E element)：将元素插入队列的尾部。如果队列已满，则调用线程会被阻塞，直到队列有空间可以插入元素为止。
     * take()：从队列的头部移除并返回元素。如果队列为空，则调用线程会被阻塞，直到队列中有元素可供取出为止。
     * offer(E element)：将元素插入队列的尾部。如果队列已满，则立即返回false，不会阻塞。
     * poll()：从队列的头部移除并返回元素。如果队列为空，则立即返回null，不会阻塞。
     * offer(E element, long timeout, TimeUnit unit)：在指定的时间内尝试将元素插入队列的尾部。如果队列已满，则调用线程会被阻塞，直到插入成功或超时。
     * poll(long timeout, TimeUnit unit)：在指定的时间内尝试从队列的头部移除并返回元素。如果队列为空，则调用线程会被阻塞，直到获取到元素或超时。
     *
     * BlockingQueue接口有多个实现类，其中最常用的是：
     *
     * ArrayBlockingQueue：基于数组实现的有界阻塞队列。
     * LinkedBlockingQueue：基于链表实现的可选有界或无界阻塞队列。
     * PriorityBlockingQueue：基于优先级堆实现的无界阻塞队列。
     * SynchronousQueue：一个没有存储空间的阻塞队列，每个插入操作必须等待相应的删除操作，反之亦然。
     *
     * 使用BlockingQueue可以简化多线程编程中的生产者-消费者模型，生产者线程可以安全地将数据插入队列，而消费者线程可以安全地从队列中获取数据，
     * 而不需要手动编写额外的同步代码。阻塞队列的阻塞操作可以有效地控制线程的等待和唤醒，实现线程间的协作和同步。
     * ==========================================================================================
     */
    private BlockingQueue<Integer> queue;

    public Producer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }
    @Override
    public void run() {
        try{
            for(int i = 0; i < 100; i++) {
                Thread.sleep(20);//模拟服务器处理的时间间隔
                queue.put(i);//将数据放入到阻塞队列
                System.out.println(Thread.currentThread().getName() + "生产：" + queue.size());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}


class Consumer implements Runnable{

    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try{
            while(true) {
                Thread.sleep(new Random().nextInt(1000));//随机睡眠一段时间，模拟用户操作的时间间隔
                queue.take();//从阻塞队列中取数据
                System.out.println(Thread.currentThread().getName() + "消费：" + queue.size());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
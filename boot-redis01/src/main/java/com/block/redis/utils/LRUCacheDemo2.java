package com.block.redis.utils;

import java.util.HashMap;
import java.util.Map;

public class LRUCacheDemo2 {

    /**
     * 构建一个Node节点，作为数据载体
     * @param <K>
     * @param <V>
     */
    class Node<K,V>{
        K k;
        V v;
        Node<K,V> prev;//前驱节点
        Node<K,V> next;//后驱节点

        public Node(){
            this.prev=this.next=null;
        }

        public Node(K k, V v) {
            this.k = k;
            this.v = v;
            this.prev = null;
            this.next = null;
        }
    }

    /**
     *构造一个双向队列，里面存放我们的Node
     */
    class DoubleLinkedList<K,V>{
        Node<K,V> head;//头结点
        Node<K,V> tail;//尾结点

        /**
         * 构造方法
         */
        public DoubleLinkedList() {
            head=new Node<>();
            tail=new Node<>();
            head.next=tail;//头结点的next指向tail
            tail.prev=head;//尾结点的prev指向头结点
        }

        /**
         * 添加到头
         */
        public void addHead(Node<K,V> node){
            node.next=head.next;//Node的next节点指向tail，就是head.next
            head.next.prev=node;//head.next.prev即尾结点的prev指向node

            node.prev=head;//node的prev指向head
            head.next=node;//head的next指向node
        }

        /**
         * 删除节点
         */
        public void removeNode(Node<K,V> node){
            node.next.prev=node.prev;//把node的下一个节点的prev指向node前一个节点
            node.prev.next=node.next;//把node的前一个节点的next指向node的下一个节点
            node.prev=null;//把node的prev设置为null,方便垃圾回收
            node.next=null;//把node的next设置为null,方便垃圾回收
        }

        /**
         * 获取最后一个节点
         * @return
         */
        public Node getLast(){
            return tail.prev;
        }
    }

    private int cacheSize;
    Map<Integer,Node<Integer,Integer>> map;
    DoubleLinkedList<Integer,Integer> doubleLinkedList;

    public LRUCacheDemo2(int cacheSize) {
        this.cacheSize = cacheSize;
        map=new HashMap<>();
        doubleLinkedList=new DoubleLinkedList<>();
    }

    public int get(int key){
        if(!map.containsKey(key)){
            return -1;
        }
        Node<Integer, Integer> node = map.get(key);
        doubleLinkedList.removeNode(node);
        doubleLinkedList.addHead(node);
        return node.v;
    }


    public void put(int key,int value){
        //如果map中包含key,就更新
        if(map.containsKey(key)){
            Node<Integer, Integer> node = map.get(key);
            node.v=value;
            map.put(key,node);
            doubleLinkedList.removeNode(node);
            doubleLinkedList.addHead(node);
        }else {
            if(map.size()==cacheSize){//坑位满了
                Node<Integer,Integer> lastNode = doubleLinkedList.getLast();
                map.remove(lastNode.k);
                doubleLinkedList.removeNode(lastNode);
            }
            //新增
            Node<Integer, Integer> newNode = new Node<>(key, value);
            map.put(key,newNode);
            doubleLinkedList.addHead(newNode);
        }

    }


    public static void main(String[] args) {
        LRUCacheDemo2 lruCacheDemo2 = new LRUCacheDemo2(5);
        lruCacheDemo2.put(1,1);
        lruCacheDemo2.put(2,2);
        lruCacheDemo2.put(3,3);
        lruCacheDemo2.put(4,4);
        lruCacheDemo2.put(5,5);
        System.out.println(lruCacheDemo2.map.keySet());
        lruCacheDemo2.put(6,6);
        System.out.println(lruCacheDemo2.map.keySet());
        lruCacheDemo2.put(3,3);
        System.out.println(lruCacheDemo2.map.keySet());
        lruCacheDemo2.put(3,3);
        System.out.println(lruCacheDemo2.map.keySet());
        lruCacheDemo2.put(3,3);
        System.out.println(lruCacheDemo2.map.keySet());
        lruCacheDemo2.put(7,7);
        System.out.println(lruCacheDemo2.map.keySet());


    }

}

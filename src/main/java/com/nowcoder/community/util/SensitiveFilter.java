package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符号
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    //初始化前缀树
    @PostConstruct //The PostConstruct annotation is used on a method that needs to be executed after dependency injection is done to perform any initialization.
    public void init() {//初始化方法，当容器实例化bean，调用构造器之后，这个方法会自动被调用
        try (
                //getResourceAsStream:Returns an input stream for reading the specified resource.
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                //InputStreamReader:a bridge from byte streams to character streams: It reads bytes and decodes them into characters using a specified charset. The charset that it uses may be specified by name or may be given explicitly, or the platform's default charset may be accepted.
                //BufferedReader:Reads text from a character-input stream, buffering characters so as to provide for the efficient reading of characters, arrays, and lines.
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));//可能会出现字符集不正确导致敏感词过滤失败，加入StandardCharsets.UTF_8
        ){
            String keyword;
            while((keyword = reader.readLine()) != null) {
                this.addKeyword(keyword);//添加到前缀树
            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    //将一个敏感词添加到前缀树中：将单词拆分成一个个字符c，然后从根节点开始往下添加
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for(int i = 0; i < keyword.length(); i++) {
            char ch = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(ch);
            if(subNode == null) {//没有这个节点才需要初始化并加入。
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(ch,subNode);
            }
            //指针指向子节点，进入下一轮循环
            tempNode = subNode;

            //设置结束标识
            if(i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 利用前缀树过滤敏感词的算法--老师的版本存在一些bug
     * 将某个
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        //判断text文本是否有空格
        if(StringUtils.isBlank(text)){
            return null;
        }
        // 工作指针--遍历前缀树
        TrieNode tempNode = rootNode;
        // 左指针
        int begin = 0;
        // 右指针
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while(begin < text.length()){
            if(position < text.length()) {
                Character c = text.charAt(position);//char基本类型 Character包装类型

                // 跳过符号
                if (isSymbol(c)) {
                    if (tempNode == rootNode) {
                        begin++;//略过符号
                        sb.append(c);
                    }
                    position++;
                    continue;
                }

                // 检查下级节点
                tempNode = tempNode.getSubNode(c);
                if (tempNode == null) {//不是敏感词
                    // 以begin开头的字符串不是敏感词
                    sb.append(text.charAt(begin));
                    // 进入下一个位置
                    position = ++begin;
                    // 重新指向根节点
                    tempNode = rootNode;
                }
                // 发现敏感词
                else if (tempNode.isKeywordEnd()) {//是敏感词
                    sb.append(REPLACEMENT);
                    begin = ++position;
                }
                // 检查下一个字符
                else {
                    position++;
                }
            }
            // position遍历越界仍未匹配到敏感词
            else{
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            }
        }
        return sb.toString();
    }

    //判断是否为符号
    private boolean isSymbol(Character ch) {//isAsciiAlphanumeric:Checks whether the character is ASCII 7 bit numeric.
        //0x2180-0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(ch) && (ch < 0x2180 || ch > 0x9FFF);
    }

    /**
     * 内部类，前缀树数据结构，用于敏感词过滤。
     * 数据结构与算法：前缀树（字典树）
     * Trie，又称字典树、单词查找树或键树，是一种树形结构，是一种哈希树的变种。
     * 典型应用是用于统计，排序和保存大量的字符串（但不仅限于字符串），所以经常被搜索引擎系统用于文本词频统计。
     * 它的优点是：利用字符串的公共前缀来减少查询时间，最大限度地减少无谓的字符串比较，查询效率比哈希树高。@pdai
     *
     * 前缀树的3个基本性质：
     * 根节点不包含字符，除根节点外每一个节点都只包含一个字符。
     * 从根节点到某一节点，路径上经过的字符连接起来，为该节点对应的字符串。
     * 每个节点的所有子节点包含的字符都不相同。
     *
     * 有哪些应用：
     * 前缀匹配、词频统计、字符串排序
     * 字符串检索，比如敏感词过滤，黑白名单等
     *
     */
    private class TrieNode {

        //关键词结束标识
        private boolean isKeywordEnd = false;

        //子节点（key是下级字符，value是下级节点）--这是前缀树的关键，本质上是m叉树
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}

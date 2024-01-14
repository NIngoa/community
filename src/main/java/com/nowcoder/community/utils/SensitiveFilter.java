package com.nowcoder.community.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SensitiveFilter {
    //替换符
    private static final String replaceStr = "**";
    //根节点
    private final TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        ) {
            String keyWord;
            while ((keyWord = bufferedReader.readLine()) != null) {
                //添加前缀树
                this.addKeyWord(keyWord);
            }
        } catch (Exception e) {
            log.error("加载敏感词文件失败:{}", e.getMessage());
        }

    }

    /**
     * 添加敏感词
     * @param keyWord
     */
    private void addKeyWord(String keyWord) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyWord.length(); i++) {
            char c = keyWord.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            //指向子节点，开始下一轮循环
            tempNode = subNode;
            if (i == keyWord.length() - 1) {
                //设置结束标识
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text
     * @return
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)){
            return null;
        }
        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;
        //结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);
            //跳过符号
            if (isSymbol(c)){
                // 若tempNode处于根节点,将此符号计入结果,让begin向下走一步
                if (tempNode==rootNode){
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间,指针3都向下走一步
                position++;
                continue;
            }
            // 检查下级节点
            tempNode= tempNode.getSubNode(c);
            // 以begin开头的字符串不是敏感词
            if (tempNode==null){
                sb.append(text.charAt(begin));
                position=++begin;
                tempNode=rootNode;
            } else if (tempNode.isKeyWordEnd()) {
                // 发现敏感词,将begin~position字符串替换掉
                sb.append(replaceStr);
                begin=++position;
                tempNode=rootNode;
            }else {
                position++;
            }
        }
        sb.append(text.substring(begin));
        return sb.toString();
    }

    //判断是否是符号
    public boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //前缀树
    private class TrieNode {
        //关键词结束标识
        private boolean isKeyWordEnd;

        //子节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
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

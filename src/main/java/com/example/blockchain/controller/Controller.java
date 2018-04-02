package com.example.blockchain.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.blockchain.dao.BlockChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Layne on 2018-3-29.
 */
@RestController
public class Controller {

    @Autowired
    private ServletContext servletContext;

    /**
     * 运行工作算法的证明来获得下一个证明，也就是所谓的挖矿
     * @return
     */
    @RequestMapping("mine")
    public Object mine(HttpSession session){
        BlockChain blockChain = BlockChain.getInstance();
        Map<String, Object> lastBlock = blockChain.lastBlock();
        long lastProof = Long.parseLong(lastBlock.get("proof") + "");
        long proof = blockChain.proofOfWork(lastProof);

        // 给工作量证明的节点提供奖励，发送者为 "0" 表明是新挖出的币
        String uuid = (String) servletContext.getAttribute("uuid");
        System.out.println("uuid ============= " + uuid);
        blockChain.newTransactions("0", uuid, 1);

        // 构建新的区块
        Map<String, Object> newBlock = blockChain.newBlock(proof, null);
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("message", "New Block Forged");
        response.put("index", newBlock.get("index"));
        response.put("transactions", newBlock.get("transactions"));
        response.put("proof", newBlock.get("proof"));
        response.put("previous_hash", newBlock.get("previous_hash"));

        // 返回新区块的数据给客户端
        return response;
    }

    /**
     * 接收并处理新的交易信息
     * @return
     */
    @RequestMapping("transactions/new")
    public Object newTransactions(HttpServletRequest req) throws IOException {
        // 返回json格式的数据给客户端
        Map<Object, Object> map = new HashMap<>();

        req.setCharacterEncoding("utf-8");
        // 读取客户端传递过来的数据并转换成JSON格式
        BufferedReader reader = req.getReader();
        String input = null;
        StringBuffer requestBody = new StringBuffer();
        while ((input = reader.readLine()) != null) {
            requestBody.append(input);
        }
        JSONObject jsonValues = JSONObject.parseObject(requestBody.toString());
        System.out.println(jsonValues);

        // 检查所需要的字段是否位于POST的data中
        String[] required = { "sender", "recipient", "amount" };
        for (String string : required) {
            if (!jsonValues.containsKey(string)) {
                // 如果没有需要的字段就返回错误信息
                map.put("code","0");
                map.put("message","Missing values");
                return "Missing values";
            }
        }

        // 新建交易信息
        BlockChain blockChain = BlockChain.getInstance();
        int index = blockChain.newTransactions(jsonValues.getString("sender"), jsonValues.getString("recipient"),
                jsonValues.getLong("amount"));

        // 返回json格式的数据给客户端
        map.put("code","400");
        map.put("message","Transaction will be added to Block " + index);
        System.out.println(map);
        return map;
    }

    /**
     * 输出整个区块链的数据
     * @return
     */
    @RequestMapping("chain")
    public Object chain(){
        BlockChain blockChain = BlockChain.getInstance();
        Map<String, Object> response = new HashMap<>();
        response.put("chain", blockChain.getChain());
        response.put("length", blockChain.getChain().size());
        return response;
    }


    /**
     * 注册节点
     *
     * @param address
     *            节点地址
     * @throws MalformedURLException
     */
    @RequestMapping("nodes/register")
    public Map<String, Object> registerNode(String address){
        BlockChain blockChain = BlockChain.getInstance();
        try {
            blockChain.registerNode(address);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("total_nodes", blockChain.getNodes());
        response.put("message","New nodes have been added");
        return response;
    }

    /**
     * 共识算法解决冲突，使用网络中最长的链. 遍历所有的邻居节点，并用上一个方法检查链的有效性， 如果发现有效更长链，就替换掉自己的链
     */
    @RequestMapping("nodes/resolve")
    public Object resolveConflicts(){
        BlockChain blockChain = BlockChain.getInstance();
        try {
            boolean b = blockChain.resolveConflicts();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("chain", blockChain.getChain());
        response.put("length", blockChain.getChain().size());
        response.put("message", "Our chain is authoritative");
        return response;
    }

}

package com.hero.sms.commands.pay;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.chain.Context;
import org.apache.commons.math3.util.Pair;

import com.google.common.base.Preconditions;
import com.hero.sms.entity.channel.PayChannel;

/**
 * 选择充值通道
 */
public class SelectedPayChannelCommand extends PayBaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        List<PayChannel> channels = (List<PayChannel>) context.get(PAY_CHANNEL_LIST);
        List<Pair<PayChannel, Integer>> weightList = new ArrayList<Pair<PayChannel, Integer>>();
        for (PayChannel channel: channels) {
            weightList.add(new Pair<PayChannel, Integer>(channel,channel.getWeight() + 1));
        }
        PayChannel payChannel = new WeightRandom<PayChannel, Integer>(weightList).random();
        if(payChannel != null){
            context.put(PAY_CHANNEL,payChannel);
            return false;
        }
        return true;
    }

    class WeightRandom<K,V extends Number> {
        private TreeMap<Double, K> weightMap = new TreeMap<Double, K>();
        public WeightRandom(List<Pair<K, V>> list) {
            Preconditions.checkNotNull(list, "list can NOT be null!");
            for (Pair<K, V> pair : list) {
                double lastWeight = this.weightMap.size() == 0 ? 0 : this.weightMap.lastKey().doubleValue();//统一转为double
                this.weightMap.put(pair.getValue().doubleValue() + lastWeight, pair.getKey());//权重累加
            }
        }
        public K random() {
            double randomWeight = this.weightMap.lastKey() * Math.random();
            SortedMap<Double, K> tailMap = this.weightMap.tailMap(randomWeight, false);
            return this.weightMap.get(tailMap.firstKey());
        }
    }
}

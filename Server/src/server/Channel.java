package server;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Channel {
    private static Channel channel = new Channel();
    private static ConcurrentHashMap<Integer, ChannelGroup> channelPointer;
    private static ConcurrentHashMap<String, CopyOnWriteArrayList<Integer>> channelIndex;
    private static int broadCastChannelIndex;

    private Channel() {
        channelPointer = new ConcurrentHashMap<>();
        channelIndex = new ConcurrentHashMap<>();

        ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        broadCastChannelIndex = channels.hashCode();
        channelPointer.put(channels.hashCode(), channels);
    }

    public static Channel getInstance() {
        return channel;
    }

    public void registerCharacter(String character) {
        CopyOnWriteArrayList<Integer> channelList = new CopyOnWriteArrayList<>();
        channelList.add(broadCastChannelIndex);
        channelIndex.put(character, channelList);
    }

    public void unRegisterCharacter(String character) {
        channelIndex.remove(character);
    }

    public Integer createNewChannelGroup() {
        ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        channelPointer.put(channels.hashCode(), channels);

        return channels.hashCode();
    }

    public ChannelGroup getBroadCastChannel() {
        return channelPointer.get(broadCastChannelIndex);
    }
}

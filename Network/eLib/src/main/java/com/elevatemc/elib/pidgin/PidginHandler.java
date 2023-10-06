package com.elevatemc.elib.pidgin;

import com.elevatemc.elib.pidgin.packet.Packet;
import com.elevatemc.elib.pidgin.packet.handler.IncomingPacketHandler;
import com.elevatemc.elib.pidgin.packet.handler.PacketExceptionHandler;
import com.elevatemc.elib.pidgin.packet.listener.PacketListener;
import com.elevatemc.elib.pidgin.packet.listener.PacketListenerData;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.Getter;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

//Credits: joeleoli
public class PidginHandler {

	public static JsonParser PARSER = new JsonParser();

	@Getter private final String channel;

	@Getter private JedisPool pool;
	@Getter private PidginPubSub pubSub;

	@Getter private List<PacketListenerData> listeners;
	@Getter private Map<Integer, Class> idToType;
	@Getter private Map<Class, Integer> typeToId;

	public PidginHandler(String channel,JedisPool jedisPool) {
		this.channel = channel;
		this.listeners = new ArrayList<>();
		this.idToType = new HashMap<>();
		this.typeToId = new HashMap<>();
		this.pool = jedisPool;
		this.pubSub = new PidginPubSub(channel);

		ForkJoinPool.commonPool().execute(() -> {

			try (Jedis jedis = this.pool.getResource()) {
				jedis.subscribe(this.pubSub,channel);
			}

		});
	}

	public void sendPacket(Packet packet) {
		this.sendPacket(packet,null);
	}

	public void sendPacket(Packet packet, PacketExceptionHandler exceptionHandler) {

		try {

			final JsonObject object = packet.serialize();

			if (object == null) {
				throw new IllegalStateException("Packet cannot generate null serialized data");
			}

			try (Jedis jedis = this.pool.getResource()) {
				jedis.publish(this.channel, packet.id() + ";" + object.toString());
			}

		} catch (Exception e) {

			if (exceptionHandler != null) {
				exceptionHandler.onException(e);
			}

		}
	}

	public Packet buildPacket(int id) {

		if (!this.idToType.containsKey(id)) {
			throw new IllegalStateException("A packet with that ID does not exist (ID: " + id + ")");
		}

		try {
			return (Packet) this.idToType.get(id).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}

		throw new IllegalStateException("Could not create new instance of packet type");
	}

	public void registerPacket(Class clazz) {
		try {

			final int id = (int) clazz.getDeclaredMethod("id").invoke(clazz.newInstance(), null);

			if (this.idToType.containsKey(id) || this.typeToId.containsKey(clazz)) {
				throw new IllegalStateException("A packet with that ID has already been registered");
			}

			this.idToType.put(id, clazz);
			this.typeToId.put(clazz, id);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void registerListener(PacketListener packetListener) {

		methodLoop:
		for (Method method : packetListener.getClass().getDeclaredMethods()) {

			if (method.getDeclaredAnnotation(IncomingPacketHandler.class) == null) {
				continue;
			}

			Class packetClass = null;

			if (method.getParameters().length > 0) {

				if (!Packet.class.isAssignableFrom(method.getParameters()[0].getType())) {
					continue;
				}

				packetClass = method.getParameters()[0].getType();
			}

			if (packetClass != null) {
				this.listeners.add(new PacketListenerData(packetListener,method,packetClass));
			}

		}
	}
}

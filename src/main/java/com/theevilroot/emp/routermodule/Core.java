package com.theevilroot.emp.routermodule;

import com.gt22.botrouter.api.ApiBinder;
import com.gt22.botrouter.api.events.listeners.SingleEventListener;
import com.gt22.botrouter.api.events.state.DisconnectEvent;
import com.gt22.botrouter.api.interfaces.IBinder;
import com.gt22.botrouter.api.interfaces.IBotDescriptor;
import com.gt22.botrouter.api.interfaces.IBotRouterModule;
import com.gt22.botrouter.api.interfaces.SpecialChannels;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class Core implements IBotRouterModule{

    public static final String MODULE_NAME = "EMP";
    public static HashMap<UUID, String> users;
    @Override
    public void init(@Nonnull  IBinder.ICommandBinder cmd, @Nonnull IBinder.IMessageBinder m, @Nonnull ExecutorService exe) {
        users = new HashMap<>();
        cmd.addCommand("AUTH", 1, (bot, args, msg) ->
        {
            if(isUUIDLogged(bot.getId())) {
                return ApiBinder.binder.messages().msg(SpecialChannels.CALLBACK, "UUID_ALREADY_LOGGED");
            }
            if(isUsernameLogged(args.get(0))) {
                return ApiBinder.binder.messages().msg(SpecialChannels.CALLBACK, "USERNAME_ALREADY_LOGGED");
            }
            register(args.get(0), bot.getId());
            System.out.println("[EMP] "+bot.getId()+" | "+args.get(0)+" logged");
            bot.getEvents().addListener(new SingleEventListener<>(DisconnectEvent.class, e -> {
                users.remove(bot.getId());
                System.out.println("[EMP] "+bot.getId()+" has been logged out");
            }));
           return ApiBinder.binder.messages().msg(SpecialChannels.CALLBACK, "DONE",args.get(0));
        });
        cmd.addCommand("USERS", 0, (bot, args, msg) -> {
           List<String> ret = new ArrayList<>();
           for(Map.Entry<UUID, String> entry : users.entrySet()) {
               ret.add(entry.getKey().toString()+":"+entry.getValue());
           }
           return ApiBinder.binder.messages().msg(SpecialChannels.CALLBACK, "EMPUSERS",(String[]) ret.toArray(new String[ret.size()]));
        });
        cmd.addCommand("SEND", 2, (bot, args, msg) -> {
            if(!isUUIDLogged(bot.getId())) {
                return ApiBinder.binder.messages().msg(SpecialChannels.CALLBACK, "YOU_NOT_LOGGED");
            }
            IBotDescriptor recipient = getBot(args.get(0));
            if (recipient == null) {
                return ApiBinder.binder.messages().msg(SpecialChannels.CALLBACK, "RECIPIENT_NOT_FOUND");
            }
                recipient.writeMessage(ApiBinder.binder.messages().msg(MODULE_NAME, "EMPMSG", users.getOrDefault(bot.getId(), bot.getId().toString()), args.get(1)));
            return ApiBinder.binder.messages().msg(SpecialChannels.CALLBACK, "DONE", users.get(recipient.getId()));
        });
    }

    @Nonnull
    @Override
    public String name() {
        return MODULE_NAME;
    }

    public static boolean isUsernameLogged(String username) {
        for(Map.Entry<UUID, String> entry : users.entrySet()) {
            if(entry.getValue().equals(username))
                return true;
        }
        return false;
    }

    public static boolean isUUIDLogged(UUID uuid) {
        for(Map.Entry<UUID, String> entry : users.entrySet()) {
            if(entry.getKey().toString().equals(uuid.toString()))
                return true;
        }
        return false;
    }

    public static boolean isUUID(String src) {
        try{
            UUID.fromString(src);
            return true;
        }catch (IllegalArgumentException e){
            return false;
        }
    }

    public static IBotDescriptor getBot(String data){
        Map<UUID, IBotDescriptor> bots = ApiBinder.binder.getBots();
        UUID uuid;
        if(isUUID(data)) {
            //If input data  - is UUID
            uuid = UUID.fromString(data);
            //System.out.println("[emp]debug: "+data+" is uuid");
        }else{
            //If it is username
            uuid = getUUID(data);
            //System.out.println("[emp]debug: "+data+" is username, uuid is "+uuid);
        }
        //System.out.println("[emp]debug: returned "+uuid);
        return uuid == null ? null : getGlobalBot(uuid);
    }

    public static IBotDescriptor getGlobalBot(UUID uuid){
        for(Map.Entry<UUID, IBotDescriptor> entry : ApiBinder.binder.getBots().entrySet()){
            if(entry.getKey().toString().equals(uuid.toString()))
                return entry.getValue();
        }
        return null;
    }

    public static UUID getUUID(String username) {
        for(Map.Entry<UUID, String> entry : users.entrySet()){
            if(entry.getValue().equals(username))
                return entry.getKey();
        }
        return null;
    }

    public static void register(String username, UUID uuid) {
        users.put(uuid, username);
    }
}

package com.beangamecore.data;

import com.beangamecore.Main;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.bukkit.plugin.Plugin;

import java.io.*;
import java.lang.reflect.Type;
import java.util.function.Predicate;

public class DataAPI {

    final Plugin p;

    public DataAPI(Plugin plugin) {
        this.p = plugin;
        this.GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(DataObject.class, new DataObjectTypeAdapter())
            .registerTypeAdapter(Predicate.class, new PredicateTypeAdapter())
            .create();
    }

    public Gson GSON;
    String readData(File file){
        try {
            FileReader fstream = new FileReader(file);
            BufferedReader reader = new BufferedReader(fstream);
            StringBuilder builder = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                builder.append(line);
            }
            reader.close();
            fstream.close();
            return builder.toString();
        } catch (IOException e){
            Main.logger().severe(e.getMessage());
            return null;
        }
    }

    void writeData(File file, String data){
        try {
            FileWriter fstream = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(fstream);
            writer.write(data);
            writer.close();
        } catch (IOException e){
            Main.logger().severe(e.getMessage());
        }
    }

    boolean createIfNonExistant(File f){
        if(!f.exists()){
            try {
                f.getParentFile().mkdirs();
                f.createNewFile();
            } catch (IOException e){
                Main.logger().severe(e.getMessage());
            }
            return false;
        }
        return true;
    }
    
    public <T extends DataObject> File createFile(Plugin plugin, String fileName, T defaultData){
        File file = new File(plugin.getDataFolder(), fileName+".json");
        boolean exists = createIfNonExistant(file);
        if(!exists){
            writeData(file, GSON.toJson(defaultData));
        }
        return file;
    }

    public <T extends DataObject> void saveData(File file, T data) {
        data.onSerialize();
        writeData(file, GSON.toJson(data));
    }

    public <T extends DataObject> T loadData(File file, Type type) {
        T data = GSON.fromJson(readData(file), type);
        if(data != null) data.onDeserialize();
        return data;
    }
    
}


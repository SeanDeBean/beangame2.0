package com.beangamecore.data;

import java.lang.reflect.Type;
import java.util.function.Predicate;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class PredicateTypeAdapter implements JsonSerializer<Predicate<?>>, JsonDeserializer<Predicate<?>> {

    @Override
    public JsonElement serialize(Predicate<?> src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString()); // Serialize as string
    }

    @Override
    public Predicate<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return obj -> true; // Deserialize as always-true predicate (or implement custom logic)
    }
    
}

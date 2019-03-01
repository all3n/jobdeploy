package com.devhc.jobdeploy.config.parser;

import com.devhc.jobdeploy.exception.DeployException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;

/**
 * Created by wanghch on 16/11/11.
 */
public class JsonArrayParser<T> implements JsonAbstractParser<JSONArray, List<T>> {

  JsonObjectParser<T> objectParser;
  static Map<Class<? extends JsonObjectParser>, JsonArrayParser> parserCaches = Maps.newHashMap();

  private JsonArrayParser() {
  }

  private JsonArrayParser(JsonObjectParser<T> objectParser) {
    this.objectParser = objectParser;
  }

  public static JsonArrayParser get(Class<? extends JsonObjectParser> objectParserClass) {
    try {
      JsonArrayParser ret = parserCaches.get(objectParserClass);
      if (ret == null) {
        JsonObjectParser objectParser = objectParserClass.newInstance();
        ret = new JsonArrayParser(objectParser);
        parserCaches.put(objectParserClass, ret);
      }
      return ret;
    } catch (Exception e) {
      throw new DeployException(e);
    }
  }

  @Override
  public List<T> parse(JSONArray a) {
    List<T> ret = Lists.newArrayList();
    if (a == null) {
      return ret;
    }
    for (int i = 0; i < a.length(); i++) {
      ret.add(objectParser.parse(a.getJSONObject(i)));
    }
    return ret;
  }
}

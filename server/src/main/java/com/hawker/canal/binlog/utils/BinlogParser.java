package com.hawker.canal.binlog.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry.*;
import com.alibaba.otter.canal.protocol.Message;

import java.util.List;

public class BinlogParser {

    private static RowChange getRowChange(Entry entry) {
        RowChange rc;
        try {
            rc = RowChange.parseFrom(entry.getStoreValue());
        } catch (Exception e) {
            throw new RuntimeException("parse event has an error , data:" + entry.toString(), e);
        }
        return rc;
    }

    private static JSONObject columnChangesFormat(List<Column> columns) {
        JSONObject jsonObj = new JSONObject(true);
        for (Column c : columns) {
            jsonObj.put(c.getName(), c.getValue());
        }
        return jsonObj;
    }


    private static JSONObject parseHeaderToJson(Entry entry) {
        Header h = entry.getHeader();

        String sourceType = h.getSourceType().name();
        String dbName = h.getSchemaName();
        String tableName = h.getTableName();
        String eventType = h.getEventType().name();
        String executeTime = String.valueOf(h.getExecuteTime());

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("sourceType", sourceType);
        jsonObj.put("dbName", dbName);
        jsonObj.put("tableName", tableName);
        jsonObj.put("eventType", eventType);
        jsonObj.put("executeTime", executeTime);
        return jsonObj;
    }

    /**
     * 如果是ddl语句，则after为sql
     */
    public static JSONArray getBinlogMsg(Message message) {
        JSONArray jsonArray = new JSONArray();

        for (Entry entry : message.getEntries()) {

            if (entry.getEntryType() != EntryType.ROWDATA)
                continue;

            RowChange rowChange = getRowChange(entry);
            String isDdl = String.valueOf(rowChange.getIsDdl());

            if (rowChange.getIsDdl()) {
                JSONObject jsonObj = parseHeaderToJson(entry);
                String afterRow = rowChange.getSql();
                jsonObj.put("isDdl", isDdl);
                jsonObj.put("afterRow", afterRow);
                jsonArray.add(jsonObj);
            } else {
                for (RowData rowData : rowChange.getRowDatasList()) {
                    JSONObject jsonObj = parseHeaderToJson(entry);

                    JSONObject beforeRow = columnChangesFormat(rowData.getBeforeColumnsList());
                    JSONObject afterRow = columnChangesFormat(rowData.getAfterColumnsList());

                    jsonObj.put("isDdl", isDdl);
                    jsonObj.put("beforeRow", beforeRow);
                    jsonObj.put("afterRow", afterRow);

                    jsonArray.add(jsonObj);
                }
            }
        }

        return jsonArray;
    }

}

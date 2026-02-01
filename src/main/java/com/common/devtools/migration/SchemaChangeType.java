package com.common.devtools.migration;

public enum SchemaChangeType {
    CREATE_TABLE,
    DROP_TABLE,
    ADD_COLUMN,
    DROP_COLUMN,
    MODIFY_COLUMN,
    ADD_INDEX,
    DROP_INDEX,
    ADD_CONSTRAINT,
    DROP_CONSTRAINT
}

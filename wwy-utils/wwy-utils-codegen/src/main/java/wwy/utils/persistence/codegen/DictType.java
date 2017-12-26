package wwy.utils.persistence.codegen;

import com.mysema.codegen.model.Type;
import org.assertj.core.util.Lists;

import java.util.List;

/**
 * @author wangxiaosan
 * @date 2017/12/26
 */
public class DictType {
    private String name;

    private String label;

    private String table;

    private String column;

    private Type dictType;

    private List<DictItemType> items = Lists.newArrayList();

    public DictType(){
        this(null, null, null, null);
    }

    public DictType(String name, String label, String table, String column) {
        this.name = name;
        this.label = label;
        this.table = table;
        this.column = column;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Type getDictType() {
        return dictType;
    }

    public void setDictType(Type dictType) {
        this.dictType = dictType;
    }

    public List<DictItemType> getItems() {
        return items;
    }

    public void setItems(List<DictItemType> items) {
        this.items = items;
    }
}

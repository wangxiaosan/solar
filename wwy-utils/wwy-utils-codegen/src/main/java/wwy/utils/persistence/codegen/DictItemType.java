package wwy.utils.persistence.codegen;

/**
 * @author wangxiaosan
 * @date 2017/12/26
 */
public class DictItemType {
    private String name;
    private Integer value;
    private String label;

    public DictItemType(){

    }

    public DictItemType(String name, Integer value, String label) {
        this.name = name;
        this.value = value;
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}

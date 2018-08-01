//package wwy.utils.persistence.codegen;
//
//import com.google.common.io.Files;
//import com.mysema.codegen.CodeWriter;
//import com.mysema.codegen.ScalaWriter;
//import com.mysema.codegen.model.ClassType;
//import com.mysema.codegen.model.SimpleType;
//import com.mysema.codegen.model.Type;
//import com.mysema.codegen.model.TypeCategory;
//import com.querydsl.codegen.*;
//import com.querydsl.sql.ColumnImpl;
//import com.querydsl.sql.ColumnMetadata;
//import com.querydsl.sql.Configuration;
//import com.querydsl.sql.SQLTemplates;
//import com.querydsl.sql.SQLTemplatesRegistry;
//import com.querydsl.sql.codegen.KeyDataFactory;
//import com.querydsl.sql.codegen.NamingStrategy;
//import com.querydsl.sql.codegen.SQLCodegenModule;
//import com.querydsl.sql.codegen.SpatialSupport;
//import com.querydsl.sql.codegen.support.PrimaryKeyData;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.annotation.Nullable;
//import javax.validation.constraints.Size;
//import java.io.File;
//import java.io.IOException;
//import java.io.StringWriter;
//import java.lang.annotation.Annotation;
//import java.nio.charset.Charset;
//import java.sql.DatabaseMetaData;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.*;
//
///**
// * SimpleMetaDataExporter exports JDBC metadata to Querydsl query types
// * <p>
// * <p>
// * Example
// * </p>
// * <p>
// * <pre>
// * SimpleMetaDataExporter exporter = new SimpleMetaDataExporter();
// * exporter.setPackageName(&quot;com.example.domain&quot;);
// * exporter.setTargetFolder(new File(&quot;target/generated-sources/java&quot;));
// * exporter.export(connection.getMetaData());
// * </pre>
// *
// * @author tiwe
// */
//public class SimpleMetaDataExporter {
//
//    private static final Logger logger = LoggerFactory
//            .getLogger(SimpleMetaDataExporter.class);
//
//    private final SQLTemplatesRegistry sqlTemplatesRegistry = new SQLTemplatesRegistry();
//
//    private final SQLCodegenModule module = new SQLCodegenModule();
//
//    private final Set<String> classes = new HashSet<String>();
//
//    private File targetFolder;
//
//    @Nullable
//    private String basePackageName;
//
//    @Nullable
//    private String schemaPattern, tableNamePattern;
//
//    @Nullable
//    private Serializer beanSerializer;
//
//    @Nullable
//    private Serializer modelSerializer;
//
//    @Nullable
//    private Serializer daoIfcSerializer;
//
//    @Nullable
//    private Serializer daoImplSerializer;
//
//    @Nullable
//    private Serializer actionFactorySerializer;
//
//    @Nullable
//    private Serializer dictSerializer;
//
//    private boolean createScalaSources = false;
//
//    private final Map<EntityType, Type> entityToWrapped = new HashMap<EntityType, Type>();
//
//    private Serializer metaDataSerializer;
//
//    private TypeMappings typeMappings;
//
//    private QueryTypeFactory queryTypeFactory;
//
//    private NamingStrategy namingStrategy;
//
//    private Configuration configuration;
//
//    private KeyDataFactory keyDataFactory;
//
//    private boolean columnAnnotations = false;
//
//    private boolean validationAnnotations = false;
//
//    private boolean schemaToPackage = false;
//
//    private String sourceEncoding = "UTF-8";
//
//    private boolean lowerCase = false;
//
//    private boolean exportTables = true;
//
//    private boolean exportViews = true;
//
//    private boolean exportAll = false;
//
//    private boolean exportPrimaryKeys = true;
//
//    private boolean exportForeignKeys = true;
//
//    private boolean exportBelongsTos = true;
//
//    private boolean exportHasManys = false;
//
//    private boolean hasVersionColumn = false;
//
//    private boolean spatial = false;
//
//    private Map<String, Map<String, Type>> registerDataTypes = new HashMap<>();
//
//    private Map<String, Map<String, DictType>> registerDictTypes = new HashMap<>();
//
//    private Map<String, String> tableModelMapper = new HashMap<>();
//
//    @Nullable
//    private String tableTypesToExport;
//
//    public SimpleMetaDataExporter() {
//    }
//
//    public void registerVoDataType(String voClassName, String property,
//                                   Type type) {
//        Map<String, Type> map = registerDataTypes.get(voClassName);
//        if (null == map) {
//            map = new HashMap<>();
//        }
//        map.put(property, type);
//        registerDataTypes.put(voClassName, map);
//    }
//
//    protected EntityType createEntityType(@Nullable String schemaName,
//                                          String tableName, final String className) {
//        EntityType classModel;
//
//        if (beanSerializer == null) {
//            String packageName = normalizePackage(module.getPackageName(),
//                    schemaName);
//            String simpleName = module.getPrefix() + className
//                    + module.getSuffix();
//            Type classTypeModel = new SimpleType(TypeCategory.ENTITY,
//                    packageName + "." + simpleName, packageName, simpleName,
//                    false, false);
//            classModel = new PEntityType(classTypeModel);
//            typeMappings.register(classModel, classModel);
//
//        } else {
//            String beanPackageName = basePackageName + ".entity";
//            String beanPackage = normalizePackage(beanPackageName, schemaName);
//            String simpleName = module.getBeanPrefix() + className
//                    + module.getBeanSuffix();
//            Type classTypeModel = new SimpleType(TypeCategory.ENTITY,
//                    beanPackage + "." + simpleName, beanPackage, simpleName,
//                    false, false);
//            classModel = new PEntityType(classTypeModel);
//
//            Type mappedType = queryTypeFactory.create(classModel);
//            entityToWrapped.put(classModel, mappedType);
//            typeMappings.register(classModel, mappedType);
//        }
//
//        classModel.getData().put("schema", schemaName);
//        classModel.getData().put("table", tableName);
//        return classModel;
//    }
//
//    protected EntityType createVOEntityType(@Nullable String schemaName,
//                                            String tableName, final String className) {
//        EntityType classModel;
//
//        if (beanSerializer == null) {
//            String packageName = normalizePackage(module.getPackageName(),
//                    schemaName);
//            String simpleName = className;
//            Type classTypeModel = new SimpleType(TypeCategory.ENTITY,
//                    packageName + "." + simpleName, packageName, simpleName,
//                    false, false);
//            classModel = new PEntityType(classTypeModel);
//            typeMappings.register(classModel, classModel);
//
//        } else {
//            String voPackageName = basePackageName + ".model";
//            String beanPackage = normalizePackage(voPackageName, schemaName);
//            String simpleName = className;
//
//            Type classTypeModel = new SimpleType(TypeCategory.ENTITY,
//                    beanPackage + "." + simpleName, beanPackage, simpleName,
//                    false, false);
//            classModel = new PEntityType(classTypeModel);
//            typeMappings.register(classModel, classModel);
//
//        }
//
//        classModel.getData().put("schema", schemaName);
//        classModel.getData().put("table", tableName);
//        return classModel;
//    }
//
//    private String normalizePackage(String packageName,
//                                    @Nullable String schemaName) {
//        if (schemaToPackage && schemaName != null) {
//            return namingStrategy.appendSchema(packageName, schemaName);
//        } else {
//            return packageName;
//        }
//    }
//
//    protected Property createProperty(EntityType classModel,
//                                      String normalizedColumnName, String propertyName,
//                                      String escapedName, Type typeModel) {
//        return new Property(classModel, propertyName, escapedName, typeModel,
//                Collections.<String>emptyList(), false);
//    }
//
//    /**
//     * Export the tables based on the given database metadata
//     *
//     * @param md
//     * @param stmt
//     * @throws SQLException
//     */
//    public void export(DatabaseMetaData md, Statement stmt) throws SQLException {
//        if (basePackageName == null) {
//            basePackageName = module.getPackageName();
//        }
//        module.bind(SQLCodegenModule.PACKAGE_NAME, basePackageName + ".query");
//
//        String beanPackageName = basePackageName + ".entity";
//        module.bind(SQLCodegenModule.BEAN_PACKAGE_NAME, beanPackageName);
//
//        if (spatial) {
//            SpatialSupport.addSupport(module);
//        }
//
//        typeMappings = module.get(TypeMappings.class);
//        queryTypeFactory = module.get(QueryTypeFactory.class);
//        metaDataSerializer = module.get(Serializer.class);
//        beanSerializer = module.get(Serializer.class,
//                SQLCodegenModule.BEAN_SERIALIZER);
//        namingStrategy = module.get(NamingStrategy.class);
//        configuration = module.get(Configuration.class);
//
//        SQLTemplates templates = sqlTemplatesRegistry.getTemplates(md);
//        if (templates != null) {
//            configuration.setTemplates(templates);
//        } else {
//            logger.info("Found no specific dialect for "
//                    + md.getDatabaseProductName());
//        }
//
//        if (beanSerializer == null) {
//            keyDataFactory = new KeyDataFactory(namingStrategy,
//                    module.getPackageName(), module.getPrefix(),
//                    module.getSuffix(), schemaToPackage);
//        } else {
//            keyDataFactory = new KeyDataFactory(namingStrategy,
//                    beanPackageName, module.getBeanPrefix(),
//                    module.getBeanSuffix(), schemaToPackage);
//        }
//
//        String[] typesArray = null;
//
//        if (tableTypesToExport != null && !tableTypesToExport.isEmpty()) {
//            List<String> types = new ArrayList<String>();
//            for (String tableType : tableTypesToExport.split(",")) {
//                types.add(tableType.trim());
//            }
//            typesArray = types.toArray(new String[types.size()]);
//        } else if (!exportAll) {
//            List<String> types = new ArrayList<String>(2);
//            if (exportTables) {
//                types.add("TABLE");
//            }
//            if (exportViews) {
//                types.add("VIEW");
//            }
//            typesArray = types.toArray(new String[types.size()]);
//        }
//
//        Map<String, String> modules = new HashMap<String, String>();
//
//        if (tableNamePattern != null && tableNamePattern.contains(",")) {
//            for (String table : tableNamePattern.split(",")) {
//                ResultSet tables = md.getTables(null, schemaPattern,
//                        null, typesArray);
//                try {
//                    while (tables.next()) {
//                        String tableName = normalize(tables.getString("TABLE_NAME"));
//                        if (tableName.matches(table)) {
//                            addModule(stmt, modules, tableName);
//                            handleTable(md, tables, stmt);
//                        }
//                    }
//                } finally {
//                    tables.close();
//                }
//            }
//        } else {
//            ResultSet tables = md.getTables(null, schemaPattern,
//                    null, typesArray);
//            try {
//                while (tables.next()) {
//                    String tableName = normalize(tables.getString("TABLE_NAME"));
//                    if (null == tableNamePattern || tableName.matches(tableNamePattern)) {
//                        addModule(stmt, modules, tableName);
//                        handleTable(md, tables, stmt);
//                    }
//                }
//            } finally {
//                tables.close();
//            }
//        }
//
//        stmt.close();
//    }
//
//    private void handleDict() {
//        try {
//            XMLConfiguration config = new XMLConfiguration("dict.xml");
//            HierarchicalConfiguration.Node root = config.getRoot();
//            if (root.hasChildren()) {
//                List<ConfigurationNode> dicts = root.getChildren("dict");
//                for (int i = 0; i < dicts.size(); i++) {
//                    String name = config.getString("dict(" + i + ")[@name]");
//                    String label = config.getString("dict(" + i + ")[@label]");
//                    String table = config.getString("dict(" + i + ")[@table]");
//                    String column = config.getString("dict(" + i + ")[@column]");
//                    Map<String, DictType> dictTypeMap = registerDictTypes.get(table);
//                    if (null == dictTypeMap) {
//                        dictTypeMap = new HashMap<>();
//                    }
//                    registerDictTypes.put(table, dictTypeMap);
//                    DictType dictType = new DictType(name, label, table, column);
//                    dictTypeMap.put(column, dictType);
//                    if (dicts.get(i).getChildrenCount() > 0) {
//                        List<ConfigurationNode> configurationNodes = dicts.get(i).getChildren("item");
//                        for (int j = 0; j < configurationNodes.size(); j++) {
//                            String itemName = config.getString("dict(" + i + ").item(" + j + ")[@name]");
//                            Integer itemValue = config.getInt("dict(" + i + ").item(" + j + ")[@value]");
//                            String itemLabel = config.getString("dict(" + i + ").item(" + j + ")[@label]");
//                            dictType.getItems().add(new DictItemType(itemName, itemValue, itemLabel));
//                        }
//                    }
//                }
//            }
//        } catch (ConfigurationException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void serializeDicts() {
//
//        handleDict();
//
//        try {
//            String dictPackageName = basePackageName + ".dict";
//            for (Map<String, DictType> map : registerDictTypes.values()) {
//                for (DictType dictType : map.values()) {
//                    String simpleName = dictType.getName();
//                    String fileSuffix = createScalaSources ? ".scala" : ".java";
//                    String modulesPath = dictPackageName.replace('.', '/') + "/"
//                            + simpleName + fileSuffix;
//                    Type classTypeModel = new SimpleType(TypeCategory.ENUM,
//                            dictPackageName + "." + simpleName, dictPackageName,
//                            simpleName, false, false);
//                    EntityType classModel = new EntityType(classTypeModel);
//                    classModel.addSupertype(new Supertype(new ClassType(
//                            DictData.class)));
//                    classModel.addProperty(new Property(classModel, "label",
//                            new ClassType(String.class)));
//                    classModel.addProperty(new Property(classModel, "value",
//                            new ClassType(Integer.class)));
//                    classModel.addAnnotation(new LabelImpl(dictType.getLabel()));
//
//                    if (null != dictSerializer) {
//                        SimpleDictSerializer dictSerializer = (SimpleDictSerializer) this.dictSerializer;
//                        dictSerializer.setItemTypes(dictType.getItems());
//                        write(this.dictSerializer, modulesPath, classModel);
//                    }
//                    dictType.setDictType(classModel);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void addModule(Statement stmt, Map<String, String> modules,
//                           String tableName) throws SQLException {
//        String normalizedTableName = namingStrategy
//                .normalizeTableName(tableName);
//        String className = namingStrategy.getClassName(normalizedTableName);
//        String tableComment = getTableComment(stmt, tableName);
//        modules.put(className, null == tableComment ? className : tableComment);
//    }
//
//    private String getTableComment(Statement stmt, String table)
//            throws SQLException {
//        ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + table);
//        String comment = null;
//        if (rs != null && rs.next()) {
//            String create = rs.getString(2);
//            comment = parse(create);
//        }
//        rs.close();
//        return comment;
//    }
//
//    String parse(String all) {
//        String comment = null;
//        int index = all.indexOf("COMMENT='");
//        if (index < 0) {
//            return "";
//        }
//        comment = all.substring(index + 9);
//        comment = comment.substring(0, comment.length() - 1);
//        return comment;
//    }
//
//    Set<String> getClasses() {
//        return classes;
//    }
//
//    private void handleColumn(EntityType classModel, String tableName,
//                              ResultSet columns) throws SQLException {
//        String columnName = normalize(columns.getString("COLUMN_NAME"));
//        String normalizedColumnName = namingStrategy
//                .normalizeColumnName(columnName);
//        int columnType = columns.getInt("DATA_TYPE");
//        String typeName = columns.getString("TYPE_NAME");
//        Number columnSize = (Number) columns.getObject("COLUMN_SIZE");
//        Number columnDigits = (Number) columns.getObject("DECIMAL_DIGITS");
//        int columnIndex = columns.getInt("ORDINAL_POSITION");
//        int nullable = columns.getInt("NULLABLE");
//        String remarks = columns.getString("REMARKS");
//        String defaultValue = columns.getString("COLUMN_DEF");
//
//        String propertyName = namingStrategy.getPropertyName(
//                normalizedColumnName, classModel);
//
//        Map<String, DictType> stringDictTypeMap = registerDictTypes.get(tableName);
//        if (null != stringDictTypeMap) {
//            DictType dictType = stringDictTypeMap.get(columnName);
//            if (null != dictType) {
//                String modelClassName = tableModelMapper.get(tableName);
//                registerVoDataType(modelClassName, propertyName, dictType.getDictType());
//            }
//        }
//
//        Class<?> clazz = configuration.getJavaType(columnType, typeName,
//                columnSize != null ? columnSize.intValue() : 0,
//                columnDigits != null ? columnDigits.intValue() : 0, tableName,
//                columnName);
//        if (clazz == null) {
//            throw new IllegalStateException("Found no mapping for "
//                    + columnType + " (" + tableName + "." + columnName + " "
//                    + typeName + ")");
//        }
//
//        TypeCategory fieldType = TypeCategory.get(clazz.getName());
//        if (Number.class.isAssignableFrom(clazz)) {
//            fieldType = TypeCategory.NUMERIC;
//        } else if (Enum.class.isAssignableFrom(clazz)) {
//            fieldType = TypeCategory.ENUM;
//        }
//        Type typeModel = new ClassType(fieldType, clazz);
//        Property property = createProperty(classModel, normalizedColumnName,
//                propertyName, propertyName, typeModel);
//        if (null != defaultValue) {
//	        property.setDefaultValue(DataTypeConvertUtils.convert(defaultValue, clazz));
//        }
//        ColumnMetadata column = ColumnMetadata.named(normalizedColumnName)
//                .ofType(columnType).withIndex(columnIndex);
//        if (nullable == DatabaseMetaData.columnNoNulls) {
//            column = column.notNull();
//        }
//        if (columnSize != null) {
//            column = column.withSize(columnSize.intValue());
//        }
//        if (columnDigits != null) {
//            column = column.withDigits(columnDigits.intValue());
//        }
//        property.getData().put("COLUMN", column);
//
//        if (columnAnnotations) {
//            property.addAnnotation(new ColumnImpl(normalizedColumnName));
//        }
//        if (validationAnnotations) {
//            if (nullable == DatabaseMetaData.columnNoNulls) {
//                NotNullImpl annotation = new NotNullImpl();
//                property.addAnnotation(annotation);
//            }
//            int size = columns.getInt("COLUMN_SIZE");
//            if (size > 0 && clazz.equals(String.class)) {
//                property.addAnnotation(new SizeImpl(0, size));
//            }
//        }
//
//        LabelImpl label = new LabelImpl(
//                StringUtils.isBlank(remarks) ? propertyName : remarks);
//        property.addAnnotation(label);
//
//        classModel.addProperty(property);
//    }
//
//    @SuppressWarnings("unchecked")
//    private void handleTable(DatabaseMetaData md, ResultSet tables,
//                             Statement stmt) throws SQLException {
//        String catalog = tables.getString("TABLE_CAT");
//        String schema = tables.getString("TABLE_SCHEM");
//        String schemaName = normalize(tables.getString("TABLE_SCHEM"));
//        String tableName = normalize(tables.getString("TABLE_NAME"));
//        String comment = getTableComment(stmt, tableName);
//        String normalizedTableName = namingStrategy
//                .normalizeTableName(tableName);
//        String className = namingStrategy.getClassName(normalizedTableName);
//        EntityType classModel = createEntityType(schemaName,
//                normalizedTableName, className);
//        EntityType modelClassModel = createVOEntityType(schemaName,
//                normalizedTableName, className);
//        tableModelMapper.put(normalizedTableName, modelClassModel.getFullName());
//
//        if (exportPrimaryKeys) {
//            // collect primary keys
//            Map<String, PrimaryKeyData> primaryKeyData = keyDataFactory
//                    .getPrimaryKeys(md, catalog, schema, tableName);
//            if (!primaryKeyData.isEmpty()) {
//                classModel.getData().put(PrimaryKeyData.class,
//                        primaryKeyData.values());
//            }
//        }
//
//        Map<String, UniqueKeyData> uniqueKeyDatas = new HashMap<>();
//        ResultSet indexInfo = md.getIndexInfo(catalog, schema, tableName, true, false);
//        while (indexInfo.next()) {
//            String indexName = indexInfo.getString("INDEX_NAME");
//            if (!"PRIMARY".equals(indexName)) {
//                UniqueKeyData uniqueKeyData = uniqueKeyDatas.get(indexName);
//                if (null == uniqueKeyData) {
//                    uniqueKeyData = new UniqueKeyData(indexName);
//                }
//                uniqueKeyDatas.put(indexName, uniqueKeyData);
//                String columnName = indexInfo.getString("COLUMN_NAME");
//                String normalizedColumnName = namingStrategy
//                        .normalizeColumnName(columnName);
//                String propertyName = namingStrategy.getPropertyName(
//                        normalizedColumnName, classModel);
//                uniqueKeyData.add(propertyName);
//            }
//        }
//        indexInfo.close();
//        if (!uniqueKeyDatas.isEmpty()) {
//            Collection<UniqueKeyData> values = uniqueKeyDatas.values();
//            classModel.getData().put(UniqueKeyData.class, values);
//            List<Unique> uniques = new ArrayList<>();
//            for (UniqueKeyData uniqueKeyData : values) {
//                UniqueImpl unique = new UniqueImpl(uniqueKeyData.getColumns().toArray(new String[uniqueKeyData.getColumns().size()]));
//                uniques.add(unique);
//            }
//            classModel.addAnnotation(new UniquesImpl(uniques.toArray(new Unique[uniques.size()])));
//        }
//
//        if (exportForeignKeys) {
//            // collect foreign keys
//            Map<String, ForeignKeyData> foreignKeyData = keyDataFactory
//                    .getImportedKeys(md, catalog, schema, tableName);
//            if (!foreignKeyData.isEmpty()) {
//                classModel.getData().put(ForeignKeyData.class,
//                        foreignKeyData.values());
//            }
//
//            // collect inverse foreign keys
//            Map<String, InverseForeignKeyData> inverseForeignKeyData = keyDataFactory
//                    .getExportedKeys(md, catalog, schema, tableName);
//            if (!inverseForeignKeyData.isEmpty()) {
//                classModel.getData().put(InverseForeignKeyData.class,
//                        inverseForeignKeyData.values());
//            }
//        }
//
//        // collect columns
//        ResultSet columns = md.getColumns(catalog, schema,
//                tableName.replace("/", "//"), null);
//        try {
//            while (columns.next()) {
//                handleColumn(classModel, tableName, columns);
//            }
//        } finally {
//            columns.close();
//        }
//
//        // serialize model
//        LabelImpl label = new LabelImpl(
//                StringUtils.isBlank(comment) ? className : comment);
//        classModel.addAnnotation(label);
//
//        Set<Property> properties = classModel.getProperties();
//        Map<String, Type> dataTypeMap = registerDataTypes.get(modelClassModel.getFullName());
//
//
//        String modelPackageName = basePackageName + ".model";
//        List<BelongsToImpl> belongsTos = new ArrayList<BelongsToImpl>();
//        for (Property property : properties) {
//            Type propertyType = property.getType();
//            boolean customType = false;
//            String propertyName = property.getName();
//            if (null != dataTypeMap) {
//                Type propertyCls = dataTypeMap.get(propertyName);
//                if (null != propertyCls) {
//                    propertyType = propertyCls;
//                    customType = true;
//                }
//            }
//
//            Collection<ForeignKeyData> foreignKeyDatas = (Collection<ForeignKeyData>) classModel
//                    .getData().get(ForeignKeyData.class);
//            boolean isForeignKey = false;
//
//            if (exportBelongsTos && null != foreignKeyDatas
//                    && !foreignKeyDatas.isEmpty()) {
//                for (ForeignKeyData foreignKey : foreignKeyDatas) {
//                    String propertyName2 = namingStrategy.getPropertyName(
//                            foreignKey.getForeignColumns().get(0), classModel);
//                    if (propertyName.equals(propertyName2)) {
//                        String simpleName = foreignKey.getType()
//                                .getSimpleName();
//                        simpleName = simpleName.substring(module.getBeanPrefix().length(), simpleName.length());
//                        propertyType = new SimpleType(modelPackageName + "."
//                                + simpleName, modelPackageName, simpleName);
//                        propertyName = propertyName2.substring(0,
//                                propertyName2.length() - 2);
//                        belongsTos.add(new BelongsToImpl(propertyName,
//                                propertyName2));
//                        isForeignKey = true;
//                        break;
//                    }
//                }
//            }
//
//            Property property2 = createProperty(modelClassModel, propertyName,
//                    propertyName, propertyName, propertyType);
//            Collection<Annotation> annotations = property.getAnnotations();
//            for (Annotation annotation : annotations) {
//                if ((isForeignKey || customType)
//                        && Size.class.equals(annotation.annotationType())) {
//                    continue;
//                }
//                property2.addAnnotation(annotation);
//            }
//            modelClassModel.addProperty(property2);
//
//
//            if (customType) {
//                property.addAnnotation(new DictDataTypeImpl((Class<? extends Enum>) propertyType.getJavaClass()));
//            }
//        }
//
//        Collection<InverseForeignKeyData> inverseForeignKeyDatas = (Collection<InverseForeignKeyData>) classModel
//                .getData().get(InverseForeignKeyData.class);
//        List<HasManyImpl> hasManys = new ArrayList<HasManyImpl>();
//        if (exportHasManys && null != inverseForeignKeyDatas
//                && !inverseForeignKeyDatas.isEmpty()) {
//            for (InverseForeignKeyData inverseForeignKeyData : inverseForeignKeyDatas) {
//                String simpleName = inverseForeignKeyData.getType()
//                        .getSimpleName();
//                simpleName = simpleName.substring(module.getBeanPrefix().length(), simpleName.length());
//                Type propertyType = new SimpleType(modelPackageName + "."
//                        + simpleName, modelPackageName, simpleName);
//                Type propertyListType = new ClassType(TypeCategory.LIST,
//                        List.class, propertyType);
//                String propertyName2 = namingStrategy.getPropertyName(
//                        inverseForeignKeyData.getTable(), classModel);
//                String propertyName = propertyName2 + "s";
//                Set<String> propertyNames = modelClassModel.getPropertyNames();
//                int index = 0;
//                while (propertyNames.contains(propertyName)) {
//                    index++;
//                    propertyName = propertyName2 + index + "s";
//                }
//                Property property2 = createProperty(modelClassModel, propertyName,
//                        propertyName, propertyName, propertyListType);
//
//                String tableComment = getTableComment(stmt,
//                        inverseForeignKeyData.getTable());
//                LabelImpl tableLabel = new LabelImpl(
//                        StringUtils.isBlank(tableComment) ? propertyName
//                                : tableComment + "列表"
//                                + (0 < index ? index : ""));
//                property2.addAnnotation(tableLabel);
//
//                modelClassModel.addProperty(property2);
//                hasManys.add(new HasManyImpl(propertyName, namingStrategy
//                        .getPropertyName(inverseForeignKeyData
//                                .getParentColumns().get(0), classModel)));
//            }
//        }
//
//        if (!belongsTos.isEmpty()) {
//            modelClassModel.addAnnotation(new BelongsTosImpl(belongsTos
//                    .toArray(new BelongsToImpl[belongsTos.size()])));
//        }
//        if (!hasManys.isEmpty()) {
//            modelClassModel.addAnnotation(new HasManysImpl(hasManys
//                    .toArray(new HasManyImpl[hasManys.size()])));
//        }
//        modelClassModel.addAnnotation(label);
//
//        serialize(classModel, modelClassModel);
//
//        logger.info("Exported " + tableName + " successfully");
//    }
//
//    private String normalize(String str) {
//        if (lowerCase && str != null) {
//            return str.toLowerCase();
//        } else {
//            return str;
//        }
//    }
//
//    private void serialize(EntityType type, EntityType modelType) {
//        try {
//            Label entityLabelAnnotation = type.getAnnotation(Label.class);
//            String entityLabel = entityLabelAnnotation.value();
//
//            String fileSuffix = createScalaSources ? ".scala" : ".java";
//
//            String beanPackageName = basePackageName + ".entity";
//            String daoPackageName = basePackageName + ".repository";
//            String actionPackageName = basePackageName + ".action";
//            String modelPackageName = basePackageName + ".model";
//
//            if (beanSerializer != null) {
//                String packageName = normalizePackage(beanPackageName,
//                        (String) type.getData().get("schema"));
//                String path = packageName.replace('.', '/') + "/"
//                        + type.getSimpleName() + fileSuffix;
//
//                Set<Property> properties = type.getProperties();
//                Property primaryKeyProperty = null;
//                boolean hasCreateTime = false;
//                boolean hasUpdateTime = false;
//                for (Property property : properties) {
//                    if ("id".equals(property.getName())) {
//                        primaryKeyProperty = property;
//                    } else if ("createTime".equals(property.getName())) {
//                        hasCreateTime = true;
//                    } else if ("updateTime".equals(property.getName())) {
//                        hasUpdateTime = true;
//                    }
//                }
//
//                PEntityType pEntityType = (PEntityType) type;
//                PEntityType pModelType = (PEntityType) modelType;
//
//                if (hasCreateTime && hasUpdateTime) {
//                    pEntityType.addSupertype(new Supertype(new ClassType(StatisticsEntity.class)));
//                }
//                Type primaryKeyPropertyType = primaryKeyProperty.getType();
//
//                Type beanSuperType = new ClassType(AbstractIdEntity.class,
//                        primaryKeyPropertyType);
//                if (hasCreateTime && hasUpdateTime) {
//                    beanSuperType = new ClassType(AbstractStatisticsEntity.class,
//                            primaryKeyPropertyType);
//                }
//                Supertype beanEntityType = new Supertype(beanSuperType);
//                pEntityType.setParentType(beanEntityType);
//
//                write(beanSerializer, path, type);
//
//                String otherPath = entityToWrapped.get(type).getFullName()
//                        .replace('.', '/')
//                        + fileSuffix;
//                write(metaDataSerializer, otherPath, type);
//
//                if (modelSerializer != null) {
//                    String modelPkgName = normalizePackage(modelPackageName,
//                            (String) modelType.getData().get("schema"));
//                    String modelPath = modelPkgName.replace('.', '/') + "/"
//                            + modelType.getSimpleName() + fileSuffix;
//                    DomainImpl domainImpl = new DomainImpl(type.getFullName());
//                    modelType.addAnnotation(domainImpl);
//
//                    Type modelSuperType = new ClassType(AbstractDataModel.class, type,
//                            primaryKeyPropertyType);
//                    Supertype modelEntityType = new Supertype(modelSuperType);
//                    pModelType.setParentType(modelEntityType);
//
//                    if (hasVersionColumn) {
//                        pModelType.addSupertype(new Supertype(new ClassType(VersionOfEntity.class)));
//                    }
//
//                    write(modelSerializer, modelPath, modelType);
//
//                }
//                // 写dao接口
//                if (daoIfcSerializer != null) {
//                    String daoIfcPkgName = normalizePackage(daoPackageName,
//                            (String) modelType.getData().get("schema"));
//                    String simpleName = modelType.getSimpleName() + "Repository";
//                    String daoIfcPath = daoIfcPkgName.replace('.', '/') + "/"
//                            + simpleName + fileSuffix;
//
//                    Type superType;
//                    if (modelSerializer != null) {
//                        superType = new ClassType(ModelQueryAndBatchUpdateRepository.class, modelType, type,
//                                primaryKeyPropertyType);
//                    } else {
//                        superType = new ClassType(DataQueryAndBatchUpdateRepository.class, type,
//                                primaryKeyPropertyType);
//                    }
//                    Supertype entityType = new Supertype(superType);
//
//                    Type classTypeModel = new SimpleType(TypeCategory.ENTITY,
//                            daoIfcPkgName + "." + simpleName, daoIfcPkgName,
//                            simpleName, false, false);
//                    PEntityType classModel = new PEntityType(classTypeModel);
//                    classModel.addSupertype(entityType);
//
//                    classModel.addAnnotation(new LabelImpl(entityLabel + "存储"));
//                    write(daoIfcSerializer, daoIfcPath, classModel);
//
//                    // 写dao实现类
//                    if (daoImplSerializer != null) {
//                        String daoImplPkgName = daoIfcPkgName + ".impl";
//                        String daoImplSimpleName = simpleName + "Impl";
//                        String daoImplPath = daoImplPkgName.replace('.', '/')
//                                + "/" + daoImplSimpleName + fileSuffix;
//
//                        Type daoImplSuperType;
//                        if (modelSerializer != null) {
//                            daoImplSuperType = new ClassType(
//                                    AbstractModelQueryAndBatchUpdateRepository.class, type,
//                                    primaryKeyPropertyType, modelType);
//                        } else {
//                            daoImplSuperType = new ClassType(
//                                    AbstractDataQueryAndBatchUpdateRepository.class, type,
//                                    primaryKeyPropertyType);
//                        }
//                        Supertype daoImplEntityType = new Supertype(
//                                daoImplSuperType);
//
//                        Type daoImplClassTypeModel = new SimpleType(
//                                TypeCategory.ENTITY, daoImplPkgName + "."
//                                + daoImplSimpleName, daoImplPkgName,
//                                daoImplSimpleName, false, false);
//                        PEntityType daoImplClassModel = new PEntityType(
//                                daoImplClassTypeModel, daoImplEntityType);
//                        daoImplClassModel
//                                .addSupertype(new Supertype(classModel));
//                        daoImplClassModel.addAnnotation(new LabelImpl(
//                                entityLabel + "存储实现"));
//                        daoImplClassModel.addAnnotation(new ValidatedImpl(new Class[]{}));
//                        write(daoImplSerializer, daoImplPath, daoImplClassModel);
//
//                    }
//
//                    // 写actionFactory实现类
//                    if (actionFactorySerializer != null) {
//                        String actionFactoryPkgName = actionPackageName;
//                        String actionFactorySimpleName = modelType.getSimpleName()
//                                + "ActionFactory";
//                        String actionFactoryPath = actionFactoryPkgName.replace(
//                                '.', '/')
//                                + "/"
//                                + actionFactorySimpleName
//                                + fileSuffix;
//
//                        Type actionFactorySuperType = new ClassType(
//                                AbstractCrudModelActionFactory.class, classTypeModel, modelType, type, primaryKeyPropertyType);
//                        Supertype actionFactoryEntityType = new Supertype(
//                                actionFactorySuperType);
//
//                        Type actionFactoryClassTypeModel = new SimpleType(
//                                TypeCategory.ENTITY, actionFactoryPkgName + "."
//                                + actionFactorySimpleName,
//                                actionFactoryPkgName, actionFactorySimpleName,
//                                false, false);
//                        PEntityType actionFactoryClassModel = new PEntityType(
//                                actionFactoryClassTypeModel,
//                                actionFactoryEntityType);
//
//                        actionFactoryClassModel.addAnnotation(new ParentModuleImpl(DataAdminModule.class));
//                        actionFactoryClassModel.addAnnotation(new LabelImpl(
//                                entityLabel + "数据管理"));
//                        actionFactoryClassModel.addAnnotation(new ValidatedImpl(new Class<?>[]{}));
//                        write(actionFactorySerializer, actionFactoryPath,
//                                actionFactoryClassModel);
//
//                    }
//                }
//            } else {
//                String packageName = normalizePackage(module.getPackageName(),
//                        (String) type.getData().get("schema"));
//                String path = packageName.replace('.', '/') + "/"
//                        + type.getSimpleName() + fileSuffix;
//                write(metaDataSerializer, path, type);
//            }
//
//        } catch (IOException e) {
//            throw new RuntimeException(e.getMessage(), e);
//        }
//    }
//
//
//    private void write(Serializer serializer, String path, EntityType type)
//            throws IOException {
//        File targetFile = new File(targetFolder, path);
//        classes.add(targetFile.getPath());
//        StringWriter w = new StringWriter();
//        CodeWriter writer = createScalaSources ? new ScalaWriter(w)
//                : new MyJavaWriter(w);
//        serializer.serialize(type, SimpleSerializerConfig.DEFAULT, writer);
//
//        // conditional creation
//        boolean generate = true;
//        byte[] bytes = w.toString().getBytes(sourceEncoding);
//        if (targetFile.exists() && targetFile.length() == bytes.length) {
//            String str = Files.toString(targetFile,
//                    Charset.forName(sourceEncoding));
//            if (str.equals(w.toString())) {
//                generate = false;
//            }
//        } else {
//            targetFile.getParentFile().mkdirs();
//        }
//
//        if (generate) {
//            Files.write(bytes, targetFile);
//        }
//    }
//
//    /**
//     * Set the schema pattern filter to be used
//     *
//     * @param schemaPattern a schema name pattern; must match the schema name as it is
//     * stored in the database; "" retrieves those without a schema;
//     * {@code null} means that the schema name should not be used to
//     * narrow the search (default: null)
//     */
//    public void setSchemaPattern(@Nullable String schemaPattern) {
//        this.schemaPattern = schemaPattern;
//    }
//
//    /**
//     * Set the table name pattern filter to be used
//     *
//     * @param tableNamePattern a table name pattern; must match the table name as it is
//     * stored in the database (default: null)
//     */
//    public void setTableNamePattern(@Nullable String tableNamePattern) {
//        this.tableNamePattern = tableNamePattern;
//    }
//
//    /**
//     * Override the configuration
//     *
//     * @param configuration override configuration for custom type mappings etc
//     */
//    public void setConfiguration(Configuration configuration) {
//        module.bind(Configuration.class, configuration);
//    }
//
//    /**
//     * Set true to create Scala sources instead of Java sources
//     *
//     * @param createScalaSources whether to create Scala sources (default: false)
//     */
//    public void setCreateScalaSources(boolean createScalaSources) {
//        this.createScalaSources = createScalaSources;
//    }
//
//    /**
//     * Set the target folder
//     *
//     * @param targetFolder target source folder to create the sources into (e.g.
//     * target/generated-sources/java)
//     */
//    public void setTargetFolder(File targetFolder) {
//        this.targetFolder = targetFolder;
//    }
//
//    /**
//     * Override the bean package name (default: packageName)
//     *
//     * @param packageName
//     */
//    public void setPackageName(@Nullable String packageName) {
//        this.basePackageName = packageName;
//    }
//
//    /**
//     * Override the name prefix for the classes (default: Q)
//     *
//     * @param namePrefix name prefix for query-types (default: Q)
//     */
//    public void setNamePrefix(String namePrefix) {
//        module.bind(CodegenModule.PREFIX, namePrefix);
//    }
//
//    /**
//     * Override the name suffix for the classes (default: "")
//     *
//     * @param nameSuffix name suffix for query-types (default: "")
//     */
//    public void setNameSuffix(String nameSuffix) {
//        module.bind(CodegenModule.SUFFIX, nameSuffix);
//    }
//
//    /**
//     * Override the bean prefix for the classes (default: "")
//     *
//     * @param beanPrefix bean prefix for bean-types (default: "")
//     */
//    public void setBeanPrefix(String beanPrefix) {
//        module.bind(SQLCodegenModule.BEAN_PREFIX, beanPrefix);
//    }
//
//    /**
//     * Override the bean suffix for the classes (default: "")
//     *
//     * @param beanSuffix bean suffix for bean-types (default: "")
//     */
//    public void setBeanSuffix(String beanSuffix) {
//        module.bind(SQLCodegenModule.BEAN_SUFFIX, beanSuffix);
//    }
//
//    public void setModelSerializer(Serializer modelSerializer) {
//        this.modelSerializer = modelSerializer;
//    }
//
//    /**
//     * Override the NamingStrategy (default: new DefaultNamingStrategy())
//     *
//     * @param namingStrategy namingstrategy to override (default: new
//     * DefaultNamingStrategy())
//     */
//    public void setNamingStrategy(NamingStrategy namingStrategy) {
//        module.bind(NamingStrategy.class, namingStrategy);
//    }
//
//    /**
//     * Set the Bean serializer to create bean types as well
//     *
//     * @param beanSerializer serializer for JavaBeans (default: null)
//     */
//    public void setBeanSerializer(@Nullable Serializer beanSerializer) {
//        module.bind(SQLCodegenModule.BEAN_SERIALIZER, beanSerializer);
//    }
//
//    /**
//     * Set the Bean serializer class to create bean types as well
//     *
//     * @param beanSerializerClass serializer for JavaBeans (default: null)
//     */
//    public void setBeanSerializerClass(
//            Class<? extends Serializer> beanSerializerClass) {
//        module.bind(SQLCodegenModule.BEAN_SERIALIZER, beanSerializerClass);
//    }
//
//    /**
//     * @param innerClassesForKeys
//     */
//    public void setInnerClassesForKeys(boolean innerClassesForKeys) {
//        module.bind(SQLCodegenModule.INNER_CLASSES_FOR_KEYS,
//                innerClassesForKeys);
//    }
//
//    /**
//     * @param columnComparatorClass
//     */
//    public void setColumnComparatorClass(
//            Class<? extends Comparator<Property>> columnComparatorClass) {
//        module.bind(SQLCodegenModule.COLUMN_COMPARATOR, columnComparatorClass);
//    }
//
//    /**
//     * @param serializerClass
//     */
//    public void setMetadataSerializerClass(
//            Class<? extends Serializer> serializerClass) {
//        module.bind(Serializer.class, serializerClass);
//    }
//
//    public void setDaoIfcSerializer(Serializer daoIfcSerializer) {
//        this.daoIfcSerializer = daoIfcSerializer;
//    }
//
//    public void setDaoImplSerializer(Serializer daoImplSerializer) {
//        this.daoImplSerializer = daoImplSerializer;
//    }
//
//    public void setActionFactorySerializer(Serializer actionFactorySerializer) {
//        this.actionFactorySerializer = actionFactorySerializer;
//    }
//
//    public void setDictSerializer(Serializer dictSerializer) {
//        this.dictSerializer = dictSerializer;
//    }
//
//    /**
//     * @param typeMappings
//     */
//    public void setTypeMappings(TypeMappings typeMappings) {
//        module.bind(TypeMappings.class, typeMappings);
//    }
//
//    /**
//     * @param columnAnnotations
//     */
//    public void setColumnAnnotations(boolean columnAnnotations) {
//        this.columnAnnotations = columnAnnotations;
//    }
//
//    /**
//     * @param validationAnnotations
//     */
//    public void setValidationAnnotations(boolean validationAnnotations) {
//        this.validationAnnotations = validationAnnotations;
//    }
//
//    /**
//     * @param sourceEncoding
//     */
//    public void setSourceEncoding(String sourceEncoding) {
//        this.sourceEncoding = sourceEncoding;
//    }
//
//    /**
//     * @param schemaToPackage
//     */
//    public void setSchemaToPackage(boolean schemaToPackage) {
//        this.schemaToPackage = schemaToPackage;
//        module.bind(SQLCodegenModule.SCHEMA_TO_PACKAGE, schemaToPackage);
//    }
//
//    /**
//     * @param lowerCase
//     */
//    public void setLowerCase(boolean lowerCase) {
//        this.lowerCase = lowerCase;
//    }
//
//    /**
//     * @param exportTables
//     */
//    public void setExportTables(boolean exportTables) {
//        this.exportTables = exportTables;
//    }
//
//    /**
//     * @param exportViews
//     */
//    public void setExportViews(boolean exportViews) {
//        this.exportViews = exportViews;
//    }
//
//    /**
//     * @param exportAll
//     */
//    public void setExportAll(boolean exportAll) {
//        this.exportAll = exportAll;
//    }
//
//    /**
//     * @param exportPrimaryKeys
//     */
//    public void setExportPrimaryKeys(boolean exportPrimaryKeys) {
//        this.exportPrimaryKeys = exportPrimaryKeys;
//    }
//
//    /**
//     * @param exportForeignKeys
//     */
//    public void setExportForeignKeys(boolean exportForeignKeys) {
//        this.exportForeignKeys = exportForeignKeys;
//    }
//
//    public void setExportBelongsTos(boolean exportBelongsTos) {
//        this.exportBelongsTos = exportBelongsTos;
//    }
//
//    public void setExportHasManys(boolean exportHasManys) {
//        this.exportHasManys = exportHasManys;
//    }
//
//    public void setHasVersionColumn(boolean hasVersionColumn) {
//        this.hasVersionColumn = hasVersionColumn;
//    }
//
//    /**
//     * Set the java imports
//     *
//     * @param imports java imports array
//     */
//    public void setImports(String[] imports) {
//        module.bind(CodegenModule.IMPORTS,
//                new HashSet<String>(Arrays.asList(imports)));
//    }
//
//    /**
//     * @param spatial
//     */
//    public void setSpatial(boolean spatial) {
//        this.spatial = spatial;
//    }
//
//    /**
//     * @param tableTypesToExport
//     */
//    public void setTableTypesToExport(String tableTypesToExport) {
//        this.tableTypesToExport = tableTypesToExport;
//    }
//
//}

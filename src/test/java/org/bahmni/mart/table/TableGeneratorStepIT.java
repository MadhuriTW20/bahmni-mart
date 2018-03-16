package org.bahmni.mart.table;

import org.bahmni.mart.AbstractBaseBatchIT;
import org.bahmni.mart.table.domain.ForeignKey;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class TableGeneratorStepIT extends AbstractBaseBatchIT {

    @Autowired
    TableGeneratorStep tableGeneratorStep;

    @Qualifier("martJdbcTemplate")
    @Autowired
    private JdbcTemplate martJdbcTemplate;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Test
    public void shouldCreateTableWithTableData() {
        TableData tableData = new TableData("tablename");

        tableData.addColumn(new TableColumn("column_one", "Integer", false, null));
        tableData.addColumn(new TableColumn("column_two", "Integer", false, null));
        tableData.addColumn(new TableColumn("column_three", "Integer", false, null));

        tableGeneratorStep.createTables(Arrays.asList(tableData));
        martJdbcTemplate.queryForList("SELECT * FROM \"tablename\"");
        List<Object> tableDataColumns = martJdbcTemplate.queryForList("SELECT column_name FROM " +
                "INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tablename' AND TABLE_SCHEMA='PUBLIC';")
                .stream().map(columns -> columns.get("COLUMN_NAME")).collect(Collectors.toList());

        assertEquals(3, tableDataColumns.size());
        assertEquals(new HashSet<>(Arrays.asList("column_one", "column_two", "column_three")),
                new HashSet<>(tableDataColumns));
    }

    @Test
    public void shouldCreateTableWithReference() {
        TableData tableData = new TableData("tablename");
        TableData referenceTableData = new TableData("foreignkeytable");

        referenceTableData.addColumn(new TableColumn("foreignkeycolumn", "Integer", true, null));

        tableData.addColumn(new TableColumn("column_one", "Integer", false, null));
        tableData.addColumn(new TableColumn("column_two", "Integer", false, null));
        ForeignKey foreignKey = new ForeignKey("foreignkeycolumn", "foreignkeytable");
        tableData.addColumn(new TableColumn("column_three", "Integer", false, foreignKey));

        tableGeneratorStep.createTables(Arrays.asList(referenceTableData, tableData));
        martJdbcTemplate.queryForList("SELECT * FROM \"tablename\"");
        List<Object> tableDataColumns = martJdbcTemplate.queryForList("SELECT column_name FROM " +
                "INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tablename' AND TABLE_SCHEMA='PUBLIC';")
                .stream().map(columns -> columns.get("COLUMN_NAME")).collect(Collectors.toList());

        assertEquals(3, tableDataColumns.size());
        assertEquals(new HashSet<>(Arrays.asList("column_one", "column_two", "column_three")),
                new HashSet<>(tableDataColumns));


        martJdbcTemplate.queryForList("SELECT * FROM \"foreignkeytable\"");
        List<Object> referenceTableDataColumns = martJdbcTemplate.queryForList("SELECT column_name FROM " +
                "INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'foreignkeytable' AND TABLE_SCHEMA='PUBLIC';")
                .stream().map(columns -> columns.get("COLUMN_NAME")).collect(Collectors.toList());

        assertEquals(1, referenceTableDataColumns.size());
        assertEquals(new HashSet<>(Arrays.asList("foreignkeycolumn")), new HashSet<>(referenceTableDataColumns));
    }
}
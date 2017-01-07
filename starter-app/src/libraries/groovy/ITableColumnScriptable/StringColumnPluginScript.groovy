import com.i2rd.dynamic.table.ITableColumn
import com.i2rd.dynamic.table.ITableRow
import com.i2rd.dynamic.table.TableColumnScriptable
import com.i2rd.dynamic.table.TableColumnType
import com.i2rd.dynamic.table.ui.AbstractITableColumnEditor
import com.i2rd.dynamic.table.ui.ITableColumnEditor
import com.i2rd.lib.Parameter
import com.i2rd.lib.util.ParameterUtil
import net.proteusframework.cms.CmsValidationException
import net.proteusframework.core.locale.TextSources
import net.proteusframework.core.notification.Notifiable
import net.proteusframework.core.script.ScriptAttributes
import net.proteusframework.ui.miwt.component.Component
import net.proteusframework.ui.miwt.component.Field
import net.proteusframework.ui.miwt.resource.CKEditorConfig

class StringColumnEditor extends AbstractITableColumnEditor
{
    def escapeHTML = false
    def rows
    def columns
    def rteConfig


    @Override
    Component getComponent(ITableColumn iTableColumn, Object value)
    {
        def field = new Field()
        if (rows instanceof Number)
            field.setDisplayHeight(rows.intValue())
        if (columns instanceof Number)
            field.setDisplayWidth(columns.intValue())
        if (rteConfig instanceof CKEditorConfig)
        {
            field.setRichEditor(true)
            field.setRichEditorConfig(rteConfig.name())
        }
        if (value != null)
            field.text = value.toString()
        field
    }

    public Object getValue(ITableColumn column, Component component) throws CmsValidationException
    {
        def content = component.text.trim()
        if (component.isRichEditor())
        {
            if (content == '<p></p>' || content == '<div></div>' || content.isEmpty())
                content = null
            else if (escapeHTML)
                content = com.i2rd.java.util.CharacterEscapeUtil.escapeForXMLParsing(content)
        }
        return content
    }

    boolean hasValue(ITableColumn column, Component component)
    {
        getValue(column, component)
    }

    // Should validate HTML for rich editor mode
    @Override
    boolean validateValue(ITableColumn iTableColumn, Component component, Notifiable componentNotifiable)
    {
        return true
    }

    void reset(ITableColumn column, Component component, Object value)
    {
        if (value == null)
            component.text = ''
        else
            component.text = value
    }

    boolean supportsColumn(ITableColumn column)
    {
        true
    }

}

class StringColumnPlugin extends TableColumnScriptable
{
    static def PN_ESCAPE = 'Escape Content (HTML)'
    static def PN_ROWS = 'Rows'
    static def PN_COLUMNS = 'Columns'
    static def PN_RICH_TEXT_EDITOR = 'Rich Text Editor'

    static def DEFAULT_ROWS = '7'
    static def DEFAULT_COLS = '40'

    def _params

    StringColumnPlugin(def ctx)
    {
        _params = ScriptAttributes._params.getAttribute(ctx)
    }

    List<? extends Parameter> getParameters()
    {
        def options = ['Plain Text']
        for (CKEditorConfig config : CKEditorConfig.values())
            options.add(config.name())

        def fieldOptions = ParameterUtil.fieldConfigurationBuilder()
            .withMissingOptionAsError(false)
            .withOption('mode', 'numeric')
            .withOption('size', 3)
        [ParameterUtil.field(PN_ROWS, TextSources.create(PN_ROWS), DEFAULT_ROWS, fieldOptions),
         ParameterUtil.field(PN_COLUMNS, TextSources.create(PN_COLUMNS), DEFAULT_COLS, fieldOptions),
         ParameterUtil.dropdown(PN_RICH_TEXT_EDITOR, TextSources.create(PN_RICH_TEXT_EDITOR), null, null, options as String[]),
         ParameterUtil.dropdown(PN_ESCAPE, TextSources.create(PN_ESCAPE), 'No', null, 'Yes', 'No'),
         ParameterUtil.checkbox('Checkboxes1', ['1','3'], '1','2','3','4','5'),
        ]
    }


    boolean supportsColumn(TableColumnType tct)
    {
        tct.getValueType() == String.class
    }

    @Override
    public ITableColumnEditor getColumnEditor(ITableRow row, ITableColumn column)
    {
        def editor = new StringColumnEditor()
        def rows = _params[PN_ROWS] ?: DEFAULT_ROWS
        if (rows.isInteger())
            editor.rows = rows.toInteger()
        def cols = _params[PN_COLUMNS] ?: DEFAULT_COLS
        if (cols.isInteger())
            editor.columns = cols.toInteger()
        if (_params[PN_ESCAPE] == 'Yes')
            editor.escapeHTML = true
        def editorConfig = _params[PN_RICH_TEXT_EDITOR]
        if (editorConfig != null)
        {
            try
            {
                editor.rteConfig = CKEditorConfig.valueOf(editorConfig)
            }
            catch (Exception e)
            {
                // Not a RTE config
            }
        }
        editor
    }
}

scriptable = new StringColumnPlugin(context)

/*
binding['Parameterized'] = [
                            'getParameters' : {[]},
                            'checkParameters' : {}
                            ] as Parameterized
if(binding[ScriptUtil.TESTING_ATTRIBUTE] == false)
{
    def columnName = binding[ITableColumnScriptable.ATTRIBUTE_TABLE_COLUMN_NAME]
    def columnType = binding[ITableColumnScriptable.ATTRIBUTE_TABLE_COLUMN_TYPE]
}
else 
    scriptable = new TableColumnScriptable();
*/
import com.i2rd.dynamic.table.DynamicTableDAO
import com.i2rd.dynamic.table.ITable
import com.i2rd.dynamic.table.ITableColumn
import com.i2rd.dynamic.table.ITableRow
import com.i2rd.dynamic.table.ITableScriptable
import com.i2rd.dynamic.table.TableColumnScriptable
import com.i2rd.dynamic.table.TableColumnType
import com.i2rd.dynamic.table.ui.ITableColumnEditor
import com.i2rd.dynamic.table.ui.RowCollectionColumnEditor
import com.i2rd.dynamic.table.ui.RowCollectionColumnEditor.EditorComponent
import com.i2rd.dynamic.table.ui.RowSelector.RowMode
import com.i2rd.lib.Parameter
import com.i2rd.lib.util.ParameterUtil
import groovy.transform.TypeChecked
import net.proteusframework.cms.CmsValidationException
import net.proteusframework.core.hibernate.dao.EntityRetriever
import net.proteusframework.core.notification.NotificationImpl
import net.proteusframework.ui.column.DataColumnTable
import net.proteusframework.ui.miwt.Action
import net.proteusframework.ui.miwt.component.Component
import net.proteusframework.ui.miwt.component.Container
import net.proteusframework.ui.miwt.component.HTMLComponent
import net.proteusframework.ui.miwt.component.PushButton
import net.proteusframework.ui.miwt.component.composite.MessageContainer
import net.proteusframework.ui.miwt.data.event.TableModelListener
import net.proteusframework.ui.miwt.event.ActionEvent
import net.proteusframework.ui.miwt.event.ActionListener
import net.proteusframework.ui.miwt.util.CommonActions
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


@TypeChecked
class RowCollectionPlugin extends TableColumnScriptable
{
    static String PN_FLAGS = "Options"

    static String FLAG_INCLUDE_EDIT = "Include edit"
    static String FLAG_INCLUDE_REMOVE = "Include remove"
    static String FLAG_INCLUDE_ADD_NEW = "Include add new"
    static String FLAG_INCLUDE_ADD_EXISTING = "Include add existing"
    static String FLAG_USE_ROW_EXPRESSION_IN_SELECTOR = "Use row expression in the 'add existing' dialog"
    static String FLAG_INCLUDE_ADD_NEW_FORM = "Include 'add new form' below the collection table"
    static String FLAG_ADD_EXISTING_INCLUDE_ADD_NEW = "Allow 'add new' from 'add existing' dialog"
    static String FLAG_ADD_EXISTING_HIDE_SELECTED = "Hide selected rows in 'add existing' dialog"
    static String FLAG_DISABLE_DRAG_N_DROP = "Disable drag 'n' drop to reorder rows (only applies to list collection type)"

    static String PN_LABEL_EDIT = "'Edit' action label"
    static String PN_LABEL_REMOVE = "'Remove' action label"
    static String PN_LABEL_ADD_NEW = "'Add new' action label"
    static String PN_LABEL_ADD_EXISTING = "'Add existing' action label"
    static String PN_MAX_ROW_COUNT = "Maximum row count (only applies to 'add new form'; blank for unlimited)"

    static String PN_COLUMN_HEADER = "Column header HTML content"
    static String PN_PRE_POPULATE_IDS = "Copy in custom DB rows with IDs (comma-separated)"

    Logger _logger
    Map<String, Object> _params

    RowCollectionPlugin(Logger logger, Map<String, Object> params)
    {
        _logger = logger;
        _params = params
    }

    List<? extends Parameter> getParameters()
    {
        [ParameterUtil.checkbox(PN_FLAGS, null, [
            FLAG_INCLUDE_REMOVE,
            FLAG_INCLUDE_ADD_EXISTING,
            FLAG_ADD_EXISTING_HIDE_SELECTED
        ],
            FLAG_INCLUDE_EDIT,
            FLAG_INCLUDE_REMOVE,
            FLAG_INCLUDE_ADD_NEW,
            FLAG_INCLUDE_ADD_EXISTING,
            FLAG_USE_ROW_EXPRESSION_IN_SELECTOR,
            FLAG_INCLUDE_ADD_NEW_FORM,
            FLAG_ADD_EXISTING_INCLUDE_ADD_NEW,
            FLAG_ADD_EXISTING_HIDE_SELECTED,
            FLAG_DISABLE_DRAG_N_DROP
        ),
         ParameterUtil.field(PN_LABEL_EDIT, null, null, null),
         ParameterUtil.field(PN_LABEL_REMOVE, null, null, null),
         ParameterUtil.field(PN_LABEL_ADD_NEW, null, null, null),
         ParameterUtil.field(PN_LABEL_ADD_EXISTING, null, null, null),
         ParameterUtil.field(PN_MAX_ROW_COUNT, null, null, null),
         ParameterUtil.html(PN_COLUMN_HEADER, null, null, null),
         ParameterUtil.field(PN_PRE_POPULATE_IDS, null, null, null),
        ]
    }


    boolean supportsColumn(TableColumnType tct)
    {
        tct.getValueType() == ITableRow.class
    }

    @Override
    public ITableColumnEditor getColumnEditor(ITableRow row, ITableColumn column)
    {
        def rowActions = []
        def entityActions = []

        def flags = _params[PN_FLAGS] as Set
        if (flags.contains(FLAG_INCLUDE_EDIT))
        {
            def action = RowCollectionColumnEditor.getEditAction()
            if (_params[PN_LABEL_EDIT]) action.putValue(Action.NAME, _params[PN_LABEL_EDIT])
            rowActions.add(action)
        }
        if (flags.contains(FLAG_INCLUDE_REMOVE))
        {
            def action = RowCollectionColumnEditor.getRemoveAction()
            if (_params[PN_LABEL_REMOVE]) action.putValue(Action.NAME, _params[PN_LABEL_REMOVE])
            rowActions.add(action)
        }

        if (flags.contains(FLAG_INCLUDE_ADD_NEW))
        {
            def action = RowCollectionColumnEditor.getAddNewAction()
            if (_params[PN_LABEL_ADD_NEW]) action.putValue(Action.NAME, _params[PN_LABEL_ADD_NEW])
            entityActions.add(action)
        }
        if (flags.contains(FLAG_INCLUDE_ADD_EXISTING))
        {
            def action = RowCollectionColumnEditor.getAddAction(
                flags.contains(FLAG_ADD_EXISTING_HIDE_SELECTED) ? RowMode.HIDE_SELECTED : RowMode.SHOW_ALL,
                flags.contains(FLAG_ADD_EXISTING_INCLUDE_ADD_NEW),
                flags.contains(FLAG_USE_ROW_EXPRESSION_IN_SELECTOR)
            )
            if (_params[PN_LABEL_ADD_EXISTING]) action.putValue(Action.NAME, _params[PN_LABEL_ADD_EXISTING])
            entityActions.add(action)
        }

        boolean includeAddNewForm = flags.contains(FLAG_INCLUDE_ADD_NEW_FORM)
        String rowLimitString = _params[PN_MAX_ROW_COUNT]
        int rowLimit = rowLimitString?.isInteger() ? rowLimitString.toInteger() : -1

        ITableColumnEditor editor = new RowCollectionColumnEditor(rowActions, entityActions)
        if (flags.contains(FLAG_DISABLE_DRAG_N_DROP))
            editor.setDragNDropEnabled(false)

        if (includeAddNewForm)
            editor = new EditorWithAddRowForm(editor, rowLimit)

        String columnHeaderHtml = _params[PN_COLUMN_HEADER]
        if (columnHeaderHtml)
            editor = new CustomHeaderEditor(editor, columnHeaderHtml)

        String prePopulateIdsString = _params[PN_PRE_POPULATE_IDS]
        if (prePopulateIdsString)
        {
            try
            {
                DynamicTableDAO tm = DynamicTableDAO.instance
                ITable table = tm.getTable(column.getType())
                List<ITableRow> defaultRows = []
                for (String token : prePopulateIdsString.split(','))
                {
                    Long id = Long.parseLong(token.trim())
                    ITableRow defaultRow = tm.getRow(table, id)
                    if (defaultRow != null)
                        defaultRows << defaultRow
                }
                editor = new PrepopulateCollectionEditor(editor, defaultRows)
            }
            catch (NumberFormatException ex)
            {
                _logger.debug(ex.message, ex);
            }
        }

        return editor
    }
}


/** Delegates to a RowCollectionColumnEditor; allows the "add new form" functionality. When configured,
 * the returned editor component has a built-in add new form (so the user doesn't have to click "add").
 * The form is hidden when the row limit is reached. */
@TypeChecked
class EditorWithAddRowForm
{
    @Delegate
    ITableColumnEditor delegate
    int rowLimit
    Container addForm

    EditorWithAddRowForm(RowCollectionColumnEditor delegate, int rowLimit)
    {
        this.delegate = delegate
        this.rowLimit = rowLimit
    }

    Component getComponent(
        final ITableColumn column,
        final Object value)
    {
        EditorComponent editorComponent = delegate.getComponent(column, value) as EditorComponent
        ITable table = DynamicTableDAO.instance.getTable(column.getType())
        DataColumnTable<ITableRow> mTable = editorComponent.getMTableComponent()
        addForm = Container.of('add_form_container')
        editorComponent.add addForm

        mTable.getModel().addTableModelListener({updateAddForm(table, mTable)} as TableModelListener)
        updateAddForm(table, mTable)
        return editorComponent
    }

    void updateAddForm(final ITable table, final DataColumnTable<ITableRow> mTable)
    {
        addForm.removeAllComponents()

        int rowCount = mTable.getModel().getRowCount()
        if (rowLimit < 0 || rowCount < rowLimit)
        {
            final DynamicTableDAO tm = DynamicTableDAO.getInstance();
            final ITableScriptable tablePlugin = tm.getPlugin(table);
            final ITableRow row = tm.createNewRow(table);
            final MessageContainer messageContainer = new MessageContainer();
            final Component editor = tablePlugin.getEditor(row);
            editor.addClassName('add_new_form')
            final PushButton ok = CommonActions.ADD.push();
            ok.addActionListener(new ActionListener() {
                @TypeChecked
                public void actionPerformed(ActionEvent ev)
                {
                    messageContainer.clearMessages();
                    if (tablePlugin.validateEditor(editor, messageContainer))
                    {
                        try
                        {
                            final ITableRow updatedRow = tablePlugin.getUpdatedTableRowFromEditor(editor, messageContainer);
                            mTable.getDefaultModel().addRow(updatedRow);
                            updateAddForm(table, mTable)
                        }
                        catch (CmsValidationException ex)
                        {
                            LogManager.getLogger(getClass()).debug('Unable to add form.', ex);
                            messageContainer.sendNotification(NotificationImpl.create(ex))
                        }
                    }
                }
            });
            addForm.add messageContainer
            addForm.add editor
            addForm.add(Container.of("actions persistence_actions", ok))
        }
    }
}

/**
 * Delegates to a RowCollectionColumnEditor; forces the first column header to take some HTML value. 
 * @author Jonathan Crosmer (jcrosmer@i2rd.com)
 */
@TypeChecked
class CustomHeaderEditor
{
    @Delegate
    ITableColumnEditor delegate
    String htmlHeader

    CustomHeaderEditor(ITableColumnEditor delegate, String htmlHeader)
    {
        this.delegate = delegate
        this.htmlHeader = htmlHeader
    }

    Component getComponent(
        final ITableColumn column,
        final Object value)
    {
        EditorComponent ec = (EditorComponent) delegate.getComponent(column, value)
        ec.getMTableComponent().getColumnModel().getColumn(0).setTableHeaderRenderer(
            new HTMLComponent(htmlHeader))

        return ec
    }

}

/**
 * Delegates to a RowCollectionColumnEditor; allows pre-populating the editor when opened.
 * Copies the rows, and adds them
 * to the delegate's EditorComponent.getMTableComponent() when the component is created.
 * @author Jonathan Crosmer (jcrosmer@i2rd.com)
 */
@TypeChecked
class PrepopulateCollectionEditor
{
    @Delegate
    ITableColumnEditor delegate
    List<ITableRow> defaultRows

    PrepopulateCollectionEditor(ITableColumnEditor delegate, List<ITableRow> defaultRows)
    {
        this.delegate = delegate
        this.defaultRows = defaultRows
    }

    Component getComponent(
        final ITableColumn column,
        final Object value)
    {
        EditorComponent ec = (EditorComponent) delegate.getComponent(column, value)

        if (!value)
        {
            List<ITableRow> copies = []
            DynamicTableDAO tm = DynamicTableDAO.instance
            EntityRetriever er = EntityRetriever.instance
            for (ITableRow row : defaultRows)
            {
                ITableRow copy = er.reattachIfNecessary(row).clone()
                copies << copy
            }
            tm.saveTableRows(copies)
            ec.getMTableComponent().getDefaultModel().setRows(copies)
        }

        return ec
    }

}


//noinspection GroovyUnusedAssignment
scriptable = new RowCollectionPlugin(_logger, _params)



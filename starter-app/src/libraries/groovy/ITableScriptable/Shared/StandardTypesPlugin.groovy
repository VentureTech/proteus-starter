import com.i2rd.dynamic.table.TableColumnType
import com.i2rd.dynamic.table.TableScriptable
import net.proteusframework.cms.label.Label
import net.proteusframework.data.filesystem.DirectoryEntity
import net.proteusframework.data.filesystem.FileEntity
import net.proteusframework.users.model.Address
import net.proteusframework.users.model.Contact
import net.proteusframework.users.model.PhoneNumber
import net.proteusframework.users.model.Principal

scriptable = new TableScriptable()
scriptable.addColumnType(TableColumnType.getTypeByClass(Address.class))
scriptable.addColumnType(TableColumnType.getTypeByClass(PhoneNumber.class))
scriptable.addColumnType(TableColumnType.getTypeByClass(Label.class))
scriptable.addColumnType(TableColumnType.getTypeByClass(Principal.class))     
scriptable.addColumnType(TableColumnType.getTypeByClass(Contact.class))
scriptable.addColumnType(TableColumnType.getTypeByClass(FileEntity.class))
scriptable.addColumnType(TableColumnType.getTypeByClass(DirectoryEntity.class))

/*
try {
println context[ITableScriptable.ATTRIBUTE_TABLE_SCHEMA]
println context[ITableScriptable.ATTRIBUTE_TABLE_NAME]
} catch(e) {
  println e
}*/

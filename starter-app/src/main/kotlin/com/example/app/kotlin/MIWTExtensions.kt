/*
 * Copyright (c) Interactive Information R & D (I2RD) LLC.
 * All Rights Reserved.
 *
 * This software is confidential and proprietary information of
 * I2RD LLC ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with I2RD.
 */

package com.example.app.kotlin

import com.google.common.base.CaseFormat
import com.i2rd.miwt.util.CSSUtil
import net.proteusframework.core.html.HTMLElement
import net.proteusframework.core.locale.TextSource
import net.proteusframework.core.locale.TextSources
import net.proteusframework.core.locale.TextSources.createTextForAny
import net.proteusframework.ui.column.DataColumnTable
import net.proteusframework.ui.column.FunctionColumn
import net.proteusframework.ui.column.TableDataColumn
import net.proteusframework.ui.miwt.HistoryElement
import net.proteusframework.ui.miwt.component.*
import net.proteusframework.ui.miwt.component.composite.CustomCellRenderer
import net.proteusframework.ui.miwt.component.composite.HistoryContainer
import net.proteusframework.ui.miwt.component.composite.editor.*
import net.proteusframework.ui.miwt.component.template.TemplateContainer
import net.proteusframework.ui.miwt.data.Column
import net.proteusframework.ui.miwt.util.CommonButtonText
import java.util.function.Supplier
import kotlin.reflect.KMutableProperty1


/**
 * Extensions for MIWT.
 * @author Russ Tennant (russ@proteus.co)
 */
fun <C : Component> C.withNameAndClass(nameAndClass: String) = apply {
    withComponentName(nameAndClass).addClassName(nameAndClass)
}

fun Any.label(text: Any): Label = Label(createTextForAny(text))
fun Any.html(text: Any): HTMLComponent = HTMLComponent(createTextForAny(text))
fun Any.h1(text: CharSequence): Label = label(text).withHTMLElement(HTMLElement.h1)
fun Any.h2(text: CharSequence): Label = label(text).withHTMLElement(HTMLElement.h2)
fun Component.dynamicText(label: Label, prop: String, textSupplier: () -> TextSource) = label.apply {
    this@dynamicText.addPropertyChangeListener(prop) { text = textSupplier() }
    text = textSupplier()
}

fun Any.instructions(text: CharSequence): Label = label(text).apply {
    htmlElement = HTMLElement.p
    addClassName(CSSUtil.CSS_INSTRUCTIONS)
}

inline fun Component.dialog(title: Any, block: Dialog.() -> Unit) {
    val dialog = Dialog(this.application, createTextForAny(title))
    dialog.block()
    dialog.isVisible = true
    this.windowManager.add(dialog)
}

inline fun HistoryContainer.push(block: () -> Component) {
    history.add(HistoryElement(block().apply { navigateBackOnClose(this) }))
}

fun <C : Container> C.addAll(vararg child: Component): C = this.apply {
    child.forEach { add(it) }
}

fun <C : TemplateContainer> C.addAll(vararg child: Component): C = this.apply {
    child.forEach { add(it) }
}

inline fun <V, P> CompositeValueEditor<V>.editor(
    prop: KMutableProperty1<V, P>,
    className: String,
    label: Any = prop.name.capitalize(),
    required: Boolean = false,
    crossinline valueEditorSupplier: () -> ValueEditor<P>): CompositeValueEditor<V> {

    @Suppress("UNCHECKED_CAST")
    val creator = Supplier {
        val ui = valueEditorSupplier()
        ui.addClassName(className)
        setLabelAndRequiredOnValueEditorIfPossible(ui, label, required)
    } as Supplier<ValueEditor<*>>

    val r = CompositeValueEditor.PropertyReader<V, P> { prop.get(it) }
    val w = CompositeValueEditor.PropertyWriter<V, P> { v, p -> prop.set(v, p!!) }

    return this.addEditorForProperty(creator, r, w, componentName)
}

fun <V, P> CompositeValueEditor<V>.editor(
    reader: (V) -> P,
    writer: (V, P) -> Unit,
    className: String,
    label: Any = TextSources.EMPTY,
    required: Boolean = false,
    valueEditorSupplier: () -> ValueEditor<P>): CompositeValueEditor<V> {

    @Suppress("UNCHECKED_CAST")
    val creator = Supplier {
        val ui = valueEditorSupplier()
        ui.addClassName(className)
        setLabelAndRequiredOnValueEditorIfPossible(ui, label, required)
    } as Supplier<ValueEditor<*>>

    val r = CompositeValueEditor.PropertyReader<V, P> { reader.invoke(it) }
    val w = CompositeValueEditor.PropertyWriter<V, P> { v, p -> writer.invoke(v, p!!) }

    return this.addEditorForProperty(creator, r, w, componentName)
}

fun <V, P> TemplateCompositeValueEditor<V>.editor(
    prop: KMutableProperty1<V, P>,
    required: Boolean = false,
    valueEditorSupplier: () -> ValueEditor<P>
): TemplateCompositeValueEditor<V> {
    val classComponentName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, prop.name)
    return editor(prop, classComponentName, classComponentName, required = required, valueEditorSupplier = valueEditorSupplier)
}

fun <V, P> TemplateCompositeValueEditor<V>.editor(
    prop: KMutableProperty1<V, P>,
    componentName: String,
    className: String,
    label: Any = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, prop.name).split('-')
        .joinToString(" ", transform = {it.capitalize()}),
    required: Boolean = false,
    valueEditorSupplier: () -> ValueEditor<P>
): TemplateCompositeValueEditor<V> {

    @Suppress("UNCHECKED_CAST")
    val creator = Supplier {
        val ui = valueEditorSupplier()
        ui.addClassName(className)
        ui.componentName = componentName
        setLabelAndRequiredOnValueEditorIfPossible(ui, label, required)
    } as Supplier<ValueEditor<*>>

    val r = CompositeValueEditor.PropertyReader<V, P> { prop.get(it) }
    val w = CompositeValueEditor.PropertyWriter<V, P> { v, p -> prop.set(v, p!!) }

    return this.addEditorForProperty(creator, r, w, componentName)
}

fun <V, P> TemplateCompositeValueEditor<V>.editor(
    reader: (V) -> P,
    writer: (V, P) -> Unit,
    componentName: String,
    className: String,
    label: Any = TextSources.EMPTY,
    required: Boolean = false,
    valueEditorSupplier: () -> ValueEditor<P>): TemplateCompositeValueEditor<V> {

    @Suppress("UNCHECKED_CAST")
    val creator = Supplier {
        val ui = valueEditorSupplier()
        ui.addClassName(className)
        ui.componentName = componentName
        setLabelAndRequiredOnValueEditorIfPossible(ui, label, required)
        ui
    } as Supplier<ValueEditor<*>>

    val r = CompositeValueEditor.PropertyReader<V, P> { reader.invoke(it) }
    val w = CompositeValueEditor.PropertyWriter<V, P> { v, p -> writer.invoke(v, p!!) }

    return this.addEditorForProperty(creator, r, w, componentName)
}

fun <V, P> TemplateCompositeValueEditor<V>.editorN(
    prop: KMutableProperty1<V, P?>,
    componentName: String,
    className: String,
    label: Any = prop.name.capitalize(),
    required: Boolean = false,
    valueEditorSupplier: () -> ValueEditor<P>
): TemplateCompositeValueEditor<V> {

    @Suppress("UNCHECKED_CAST")
    val creator = Supplier {
        val ui = valueEditorSupplier()
        ui.addClassName(className)
        ui.componentName = componentName
        setLabelAndRequiredOnValueEditorIfPossible(ui, label, required)
    } as Supplier<ValueEditor<*>>

    val r = CompositeValueEditor.PropertyReader<V, P> { prop.get(it) }
    val w = CompositeValueEditor.PropertyWriter<V, P> { v, p -> prop.set(v, p) }

    return this.addEditorForProperty(creator, r, w, componentName)
}

fun <V, P> TemplateCompositeValueEditor<V>.editorN(
    reader: (V) -> P?,
    writer: (V, P?) -> Unit,
    componentName: String,
    className: String,
    label: Any? = null,
    required: Boolean = false,
    valueEditorSupplier: () -> ValueEditor<P>): TemplateCompositeValueEditor<V> {

    @Suppress("UNCHECKED_CAST")
    val creator = Supplier {
        val ui = valueEditorSupplier()
        ui.addClassName(className)
        ui.componentName = componentName
        setLabelAndRequiredOnValueEditorIfPossible(ui, label, required)
    } as Supplier<ValueEditor<*>>

    val r = CompositeValueEditor.PropertyReader<V, P> { reader.invoke(it) }
    val w = CompositeValueEditor.PropertyWriter<V, P> { v, p -> writer.invoke(v, p) }

    return this.addEditorForProperty(creator, r, w, componentName)
}

inline fun <V> PropertyEditor<V>.persist(crossinline block: (V) -> Boolean): Boolean = this.persist { it -> block(it!!) }

fun Any.toTextSource(): TextSource = TextSources.createTextForAny(this)!!
fun CharSequence.toTextSource(): TextSource = TextSources.createText(this)!!

inline fun <reified V> ComboBoxValueEditor<V>.cellRenderer(
    emptyValue: Any = CommonButtonText.PLEASE_SELECT,
    crossinline function: (V) -> Any): ComboBoxValueEditor<V> = this.apply {
    setCellRenderer(CustomCellRenderer(emptyValue) { obj -> function(obj as V) })
}

inline fun <reified V> ListComponentValueEditor<V>.cellRenderer(
    emptyValue: Any = TextSources.EMPTY,
    crossinline function: (V) -> Any): ListComponentValueEditor<V> = this.apply {
    (valueComponent as ListComponent).cellRenderer = CustomCellRenderer(emptyValue) { obj -> function(obj as V) }
}

inline fun <reified P> Column.cellRenderer(
    emptyValue: Any = TextSources.EMPTY,
    crossinline function: (P) -> Any): Column = this.apply {
    tableCellRenderer = CustomCellRenderer(emptyValue) { obj -> function(obj as P) }
}

fun Column.cellRenderer(
    cellRenderer: TableCellRenderer): Column = this.apply {
    tableCellRenderer = cellRenderer
}

inline fun <reified V, reified P> DataColumnTable<V>.cellRenderer(
    col: FunctionColumn<V, P>,
    emptyValue: Any = TextSources.EMPTY,
    crossinline function: (P) -> Any): DataColumnTable<V> = this.apply {
    getUIColumn(col)?.cellRenderer<P>(emptyValue, function)
}

inline fun <reified V> DataColumnTable<V>.cellRenderer(
    col: TableDataColumn,
    cellRenderer: TableCellRenderer) = this.apply {
    getUIColumn(col)?.cellRenderer(cellRenderer)
}

abstract class ComponentProperty<P>(val name: String)

inline fun <reified V> Component.addPropertyChangeListener(prop: ComponentProperty<V>, crossinline listener: (V, V) -> Unit) {
    addPropertyChangeListener(prop.name) {
        val old = it.oldValue
        val new = it.newValue
        if (old is V && new is V) listener(old, new)
    }
}


fun <P> setLabelAndRequiredOnValueEditorIfPossible(
    ui: ValueEditor<P>,
    label: Any?,
    required: Boolean) : ValueEditor<P> {
    if (ui is AbstractSimpleValueEditor) {
        if (label != null) ui.setLabel(createTextForAny(label))
        if (required) ui.setRequiredValueValidator()
    } else if (ui is BooleanValueEditor) {
        ui.label = createTextForAny(label)
    }
    return ui
}
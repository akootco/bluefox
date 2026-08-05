@file:Suppress("UnstableApiUsage")

package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.extensions.withDisplayName
import co.akoot.plugins.bluefox.util.Color
import co.akoot.plugins.bluefox.util.error
import co.akoot.plugins.bluefox.util.plus
import co.akoot.plugins.bluefox.util.text
import co.akoot.plugins.bluefox.util.width
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

fun dialog(block: DialogBuilder.() -> Unit): Dialog {
    return DialogBuilder()
        .apply(block)
        .build()
}

data class Pixels(val value: Int)

val Int.px: Pixels get() = Pixels(this)

class DialogBuilder {
    private var title: Component = Component.text("Custom Menu!")
    private val buttons: MutableList<ActionButton> = mutableListOf()
    private val bodies: MutableList<DialogBody> = mutableListOf()
    private val inputs: MutableList<DialogInput> = mutableListOf()

    private var externalTitle: Component? = null
    private var closeWithEscape: Boolean = false
    private var afterAction: DialogBase.DialogAfterAction = DialogBase.DialogAfterAction.CLOSE
    private var columns: Int = 3

    private var defaultPadding: Pixels = 12.px
    private var defaultIconWidth: Int = 16
    private var defaultIconHeight: Int = 16

    fun title(title: Component): DialogBuilder {
        this.title = title
        return this
    }

    fun defaultPadding(padding: Pixels): DialogBuilder {
        this.defaultPadding = padding
        return this
    }

    fun defaultPadding(padding: Int): DialogBuilder {
        this.defaultPadding = padding.px
        return this
    }

    fun defaultIconWidth(width: Int): DialogBuilder {
        this.defaultIconWidth = width
        return this
    }

    fun defaultIconHeight(height: Int): DialogBuilder {
        this.defaultIconHeight = height
        return this
    }

    fun message(width: Int, component: Component): DialogBuilder {
        bodies.add(DialogBody.plainMessage(component, width.coerceIn(1, 1024)))
        return this
    }

    fun message(width: Int, text: String): DialogBuilder {
        message(width, text(text))
        return this
    }

    @JvmName("componentMessageWithPadding")
    fun message(component: Component, padding: Pixels = defaultPadding): DialogBuilder =
        message(component.width(padding.value), component)

    @JvmName("textMessageWithPadding")
    fun message(text: String, padding: Pixels = defaultPadding): DialogBuilder =
        message(text.width(padding.value), text)

    fun externalTitle(title: Component): DialogBuilder {
        externalTitle = title
        return this
    }

    fun closeWithEscape(value: Boolean): DialogBuilder {
        closeWithEscape = value
        return this
    }

    fun afterAction(action: DialogBase.DialogAfterAction): DialogBuilder {
        afterAction = action
        return this
    }

    fun columns(size: Int): DialogBuilder {
        columns = size.coerceAtLeast(1)
        return this
    }

    fun slider(
        width: Int,
        key: String,
        label: Component,
        range: ClosedFloatingPointRange<Float>,
        initial: Float = 1f,
        step: Float = 1f,
    ): DialogBuilder {
        inputs.add(
            DialogInput.numberRange(key, label, range.start, range.endInclusive)
                .initial(initial)
                .step(step.coerceAtLeast(0f))
                .width(width.coerceIn(1, 1024))
                .build()
        )
        return this
    }

    @JvmName("sliderWithPadding")
    fun slider(
        key: String,
        label: Component,
        range: ClosedFloatingPointRange<Float>,
        initial: Float = 1f,
        step: Float = 1f,
        padding: Pixels = defaultPadding,
    ): DialogBuilder = slider(
        label.width(padding.value) + "${range.endInclusive}".width(padding.value), key, label, range, initial, step
    )

    // why ts not a drop down??
    fun select(
        key: String,
        label: Component,
        options: Map<String, Component>,
        initial: String? = null
    ): DialogBuilder {
        inputs.add(
            DialogInput.singleOption(
                key,
                label,
                options.map { (option, display) ->
                    SingleOptionDialogInput.OptionEntry.create(
                        option,
                        display,
                        option == initial
                    )
                }
            ).build()
        )
        return this
    }

    fun select(
        key: String,
        label: Component,
        options: List<String>,
        initial: String? = null,
    ): DialogBuilder = select(
        key, label, options.associateWith { it.text }, initial
    )

    fun booleanSelect(
        key: String,
        label: Component,
        initial: Boolean = false,
        trueLabel: String = "Enabled",
        falseLabel: String = "Disabled",
    ): DialogBuilder = select(
        key,
        label,
        mapOf(
            "true" to (Color.May + trueLabel),
            "false" to falseLabel.error
        ),
        initial.toString()
    )

    // idk what ts means by value in template, oh well
    fun toggle(
        key: String,
        label: Component,
        initial: Boolean = false,
        onTrue: String = "Enabled",
        onFalse: String = "Disabled"
    ): DialogBuilder {
        inputs.add(DialogInput.bool(key, label, initial, onTrue, onFalse))
        return this
    }

    fun textInput(
        width: Int,
        key: String,
        label: Component,
        initial: String = "",
        maxLines: Int = 1,
        maxLength: Int = 256,
        height: Int? = null
    ): DialogBuilder {
        inputs.add(
            DialogInput.text(
                key,
                width.coerceIn(1, 1024),
                label,
                true,
                initial,
                maxLength.coerceAtLeast(1),
                TextDialogInput.MultilineOptions.create(maxLines.coerceAtLeast(1), height?.coerceIn(1, 512))
            )
        )
        return this
    }

    @JvmName("textInputWithPadding")
    fun textInput(
        key: String,
        label: Component,
        initial: String = "",
        maxLines: Int = 1,
        maxLength: Int = 256,
        padding: Pixels = defaultPadding,
        height: Int? = null
    ): DialogBuilder = textInput(
        label.width(padding.value), key, label, initial, maxLines, maxLength, height
    )

    fun icon(
        width: Int,
        height: Int,
        item: ItemStack?,
        description: Component? = null,
        showDecorations: Boolean = true,
        showTooltip: Boolean = true,
        errorItem: ItemStack = ItemStack(Material.BARRIER)
            .withDisplayName(error("oops, this item doesn't exist!"))
    ): DialogBuilder {
        val desc = description?.let { DialogBody.plainMessage(it) }
        bodies.add(DialogBody.item(item ?: errorItem, desc, showDecorations, showTooltip, width, height))
        return this
    }

    @JvmName("iconWithCustomPadding")
    fun icon(
        item: ItemStack?,
        description: Component? = null,
        verticalPadding: Pixels = 0.px,
        horizontalPadding: Pixels = 0.px,
        showDecorations: Boolean = true,
        showTooltip: Boolean = true,
        errorItem: ItemStack = ItemStack(Material.BARRIER)
            .withDisplayName(error("oops, this item doesn't exist!"))
    ): DialogBuilder = icon(
        defaultIconWidth + horizontalPadding.value,
        defaultIconHeight + verticalPadding.value,
        item,
        description,
        showDecorations,
        showTooltip,
        errorItem
    )

    @JvmName("iconWithPadding")
    fun icon(
        item: ItemStack?,
        description: Component? = null,
        padding: Pixels = 0.px,
        showDecorations: Boolean = true,
        showTooltip: Boolean = true,
        errorItem: ItemStack = ItemStack(Material.BARRIER)
            .withDisplayName(error("oops, this item doesn't exist!"))
    ): DialogBuilder =
        icon(defaultIconWidth + padding.value, defaultIconHeight + padding.value, item, description, showDecorations, showTooltip, errorItem)

    fun item(material: Material, displayName: Component? = null): ItemStack {
        return ItemStack(material).apply {
            displayName?.let { withDisplayName(it) }
        }
    }

    // apparently this is the method to avoid using the CustomClickEvent
    fun button(
        width: Int,
        label: Component,
        action: (Player, DialogResponseView) -> Unit
    ): DialogBuilder {
        buttons.add(
            ActionButton.create(
                label,
                Component.empty(),
                width,
                DialogAction.customClick(
                    { view, audience ->
                        if (audience is Player) {
                            action(audience, view)
                        }
                    },
                    ClickCallback.Options.builder().build()
                )
            )
        )
        return this
    }

    @JvmName("buttonWithPadding")
    fun button(
        label: Component,
        padding: Pixels = defaultPadding,
        action: (Player, DialogResponseView) -> Unit
    ): DialogBuilder = button(label.width(padding.value), label, action)

    fun cancelButton(label: Component = error("Cancel"), padding: Pixels = defaultPadding) =
        button(label, padding) { _, _ -> }

    fun cancelButton(label: Component = error("Cancel"), width: Int) = button(width, label) { _, _ -> }
    fun cancelButton(label: String, padding: Pixels = defaultPadding) = button(error(label), padding) { _, _ -> }
    fun cancelButton(label: String, width: Int) = button(width, error(label)) { _, _ -> }

    fun build(): Dialog {
        return Dialog.create { builder ->
            builder.empty()
                .base(
                    DialogBase.create(
                        title,
                        externalTitle,
                        closeWithEscape,
                        false,
                        afterAction,
                        bodies,
                        inputs
                    )
                ).apply {
                    if (buttons.isNotEmpty() || inputs.isNotEmpty()) {
                        type(DialogType.multiAction(buttons).columns(columns).build())
                    } else {
                        type(DialogType.notice())
                    }
                }
        }
    }
}
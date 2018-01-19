package net.kirkstork.reproducers

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class CheckboxApp : App(MainView::class)

class Customer(id: Int, lastName: String, firstName: String, selected: Boolean = false) {
	val lastNameProperty = SimpleStringProperty(this, "lastName", lastName)
	var lastName by lastNameProperty
	val firstNameProperty = SimpleStringProperty(this, "firstName", firstName)
	var firstName by firstNameProperty
	val idProperty = SimpleIntegerProperty(this, "id", id)
	var id by idProperty
	val selectedProperty = SimpleBooleanProperty(this, "selected", selected)
	var selected by selectedProperty
}

class CustomerController : Controller() {
	val customers = listOf(
			Customer(1, "Marley", "John"),
			Customer(2, "Schmidt", "Ally"),
			Customer(3, "Johnson", "Eric"),
			Customer(4, "Clark", "Sam"),
			Customer(5, "Svensonsson", "Bjorn")
	).observable()
}

class MainView : View("Checkbox Reproducer") {

	val controller: CustomerController by inject()
	var tableViewEditModel: TableViewEditModel<Customer> by singleAssign()

	override val root = vbox(20) {
		textarea {
			isEditable = false
			text = """To reproduce:
				| - select checkbox for John Marly - observe dirty flag.
				| - click PRINT - observe new value for selected in both
				|   (like a commit has happened)
				| - click COMMIT, the PRINT - observe dirty flag gone,
				|   but no new changes to item (they already were recorded
				|   before the commit)
				| - select checkbox for Ally Schmidt - observe box checked, but no
				|   dirty flag.
				| - click PRINT - observe no changes to Ally Schmidt
				| - click COMMIT, then PRINT - observe the commit had no effect.
				| - double click Eric's name and edit it - Press ENTER and
				|   observe change along with dirty flag
				| - double click Johnson and edit it.  Click some other cell and
				|   observe value returns to original. (it should be dirty with new
				|   value).
				| - double click Johnson and edit it.  Press TAB and observe new
				|   new value but no dirty flag.
				| - PRINT - observe the edit of first name appears to have been
				|   committed, but not the edit to the last name
				| - COMMIT - observe first name dirty flag gone with expected value
				|   observe last name edit revert to original value.
				| - PRINT - observe first name edit persisted but not last name
				| - Edit Clark and press ENTER.  Then check the box in that same row.
				|   observe no dirty flag for the checkbox.
				| - PRINT - observe name edit as expected but no change to selection state.
				| - COMMIT - observe checkbox reverts to unchecked, dirty flag on last name clears
				| - PRINT - observe the commit had no effect.
				| """.trimMargin("|")
		}
		tableview(controller.customers) {
			isEditable = true

			column("SELECTED", Customer::selectedProperty).useCheckbox(true)
			column("ID",Customer::idProperty)
			column("FIRST NAME", Customer::firstNameProperty).makeEditable()
			column("LAST NAME", Customer::lastNameProperty).makeEditable()

			enableCellEditing() //enables easier cell navigation/editing
			enableDirtyTracking() //flags cells that are dirty

			tableViewEditModel = editModel

		}

		button("COMMIT").action {
			tableViewEditModel.items.asSequence()
//				.filter { it.value.isDirty }
				.forEach {
					println("Committing ${it.key}")
					it.value.commit()
				}
		}

		button("Print") {
			action {
				controller.customers.forEach{
					with(it) {
						println("by kotlin prop - $id: $selected ($firstName $lastName)")
						println("by javafx prop - ${idProperty.value}: ${selectedProperty.value} (${firstNameProperty.value} ${lastNameProperty.value})")
					}
				}
			}
		}
	}
}
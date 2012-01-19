dojo.provide("dojo.widget.validate.UsTextbox");

dojo.require("dojo.widget.validate.ValidationTextbox");
dojo.require("dojo.validate.us");

dojo.widget.defineWidget(
	"dojo.widget.validate.UsStateTextbox",
	dojo.widget.validate.ValidationTextbox,
	{
		// summary: a Textbox which tests for a United States state abbreviation
		// allowTerritories  Allow Guam, Puerto Rico, etc.  Default is true.
		// allowMilitary     Allow military 'states', e.g. Armed Forces Europe (AE). Default is true.

		mixInProperties: function(/*Object*/localProperties){
			// summary: see dojo.widget.Widget

			// Initialize properties in super-class.
			dojo.widget.validate.UsStateTextbox.superclass.mixInProperties.apply(this, arguments);

			// Get properties from markup attributes, and assign to flags object.
			if(localProperties.allowterritories){
				this.flags.allowTerritories = (localProperties.allowterritories == "true");
			}
			if(localProperties.allowmilitary){
				this.flags.allowMilitary = (localProperties.allowmilitary == "true");
			}
		},

		isValid: function(){
			// summary: see dojo.widget.validate.ValidationTextbox
			return dojo.validate.us.isState(this.textbox.value, this.flags);
		}
	}
);

/*
  ****** UsZipTextbox ******

  A subclass of ValidationTextbox.
  Over-rides isValid to test if input is a US zip code.
  Validates zip-5 and zip-5 plus 4.
*/
dojo.widget.defineWidget(
	"dojo.widget.validate.UsZipTextbox",
	dojo.widget.validate.ValidationTextbox,
	{
		// summary: a Textbox which tests for a United States postal code
		isValid: function(){
			// summary: see dojo.widget.validate.ValidationTextbox
			return dojo.validate.us.isZipCode(this.textbox.value);
		}
	}
);

dojo.widget.defineWidget(
	"dojo.widget.validate.UsSocialSecurityNumberTextbox",
	dojo.widget.validate.ValidationTextbox,
	{
		// summary: a Textbox which tests for a United States Social Security number
		isValid: function(){
			// summary: see dojo.widget.validate.ValidationTextbox
			return dojo.validate.us.isSocialSecurityNumber(this.textbox.value);
		}
	}
);

dojo.widget.defineWidget(
	"dojo.widget.validate.UsPhoneNumberTextbox",
	dojo.widget.validate.ValidationTextbox,
	{
		// summary: a Textbox which tests for a United States 10-digit telephone number, extension is optional.

		isValid: function(){
			// summary: see dojo.widget.validate.ValidationTextbox
			return dojo.validate.us.isPhoneNumber(this.textbox.value);
		}
	}
);

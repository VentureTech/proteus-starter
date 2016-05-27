jQuery(function () {

	$('form.miwt-form').each(function (idx, form) {
		//noinspection JSUnusedGlobalSymbols
		form.submit_options = {
			postUpdate: function () {
				window.scrollTo(0,0);
			}
		};
	});

});
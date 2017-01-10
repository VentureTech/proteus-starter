function enableTooltips(ctx) {
	var $ctx = !!ctx ? $(ctx.parentNode || document) : $(ctx || document);

	if(!('ontouchstart' in window)) {
		$ctx.find(".tooltips").each(function (idx, el) {
			var $el = $(el);
			if ($el.hasClass("menu-component")) {
				$el.find("a[title]").tooltip();
			} else {
				$el.tooltip();
			}
		});
		$ctx.find('[data-toggle="tooltip"]').tooltip();
		$ctx.find('[data-toggle="popover"]').popover({
			trigger: 'hover'
		});
	}

}
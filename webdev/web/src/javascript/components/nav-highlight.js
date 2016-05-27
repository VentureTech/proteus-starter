
function navHighlight() {

	$('.highlight-toc').scroll(function(event) {

		//Highlight nav on scroll / click
		var scrollPos = $(document).scrollTop();
		$('section.toc .toc-entry a').each(function () {
			var currLink = $(this);
			var currLinkParent = $(currLink).parent();
			var refElement = $(currLink.attr("href"));
			if (refElement.position().top - 280 <= scrollPos && refElement.position().top + refElement.height() > scrollPos) {
				$('section.toc .toc-entry a').removeClass("active");
				currLinkParent.addClass("active");
			}
			else{
				currLinkParent.removeClass("active");
			}
		});

	});

	//smoothscroll
	$('section.toc .toc-entry a').click( function (e) {

		$('a').each(function () {
			$(this).removeClass('active');
		});
		$(this).addClass('active');

	});

}
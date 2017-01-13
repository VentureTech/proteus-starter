var SIDE_BAR_CLOSED = "sidebar-closed";
var SIDE_BAR_OPEN = "sidebar-open";
/** PUT THIS IN THE HTML AT THE TOP OF THE PAGE
 <script>
 var SIDE_BAR_CLOSED = "sidebar-closed";
 var SIDE_BAR_OPEN = "sidebar-open";
 function openSidebar(open) {
	var $body = $("#body-wrapper");
	sessionStorage[SIDE_BAR_OPEN] = open;
	if (open) {
		$body.removeClass(SIDE_BAR_CLOSED).addClass(SIDE_BAR_OPEN);
	}
	else {
		$body.addClass(SIDE_BAR_CLOSED).removeClass(SIDE_BAR_OPEN);
	}
}
 if (sessionStorage[SIDE_BAR_OPEN] !== undefined) {
	openSidebar(sessionStorage[SIDE_BAR_OPEN] === "true");
}
 </script>
 */
function responsiveView() {
	var wSize = $(window).width();
	//noinspection JSUnresolvedVariable - SIDE_BAR_OPEN defined in HTML document
	if (sessionStorage[SIDE_BAR_OPEN] === undefined) {
		//noinspection JSUnresolvedFunction - defined in HTML document
		openSidebar(!(wSize <= 768));
	}
}

jQuery(function(){
	var w = window, $w = $(w);
	$w.on('load', responsiveView);
	$('.fa-bars').click(function () {
		var $body = $("#body-wrapper"), open = !$body.hasClass("sidebar-open");
		//noinspection JSUnresolvedFunction - defined in HTML document
		openSidebar(open);
	});

	var $body = $("#body-wrapper");
	if ($body.length === 0) return;
	w.addEventListener("orientationchange", function () {
		if (w.orientation === 0) {
			//noinspection JSUnresolvedFunction - defined in HTML document
			openSidebar(false);
		}
	}, false);
	//noinspection JSUnresolvedVariable - SIDE_BAR_OPEN defined in HTML document
	if (w.orientation && w.orientation === 0 && sessionStorage[SIDE_BAR_CLOSED] === undefined) {
		//noinspection JSUnresolvedFunction - defined in HTML document
		openSidebar(false);
	}
	//noinspection JSUnresolvedVariable - SIDE_BAR_CLOSED defined in HTML document
	if ($w.width() < 600 && sessionStorage[SIDE_BAR_CLOSED] === undefined) {
		//noinspection JSUnresolvedFunction - defined in HTML document
		openSidebar(false);
	}
});
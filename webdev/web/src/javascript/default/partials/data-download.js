/**
 * Add download attribute without a file name if there is a corresponding data attribute.
 * @param ctx context or null.
 */
function handleDataDownload (ctx) {
	var $ctx = $(ctx || document)
	$ctx.find('[data-download]').each(function (idx, el) {
		el.setAttribute('download', '')
	})
	if (!!ctx && ctx.hasAttribute('[data-download]')) ctx.setAttribute('download', '')
}
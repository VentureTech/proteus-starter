[#-- Namespace --]
[#assign default_namespace="util"]

[#-- uncomment this if you need numbers with more precision, ex: lat/lon --]
[#-- setting number_format="0.#############" --]

[#--
	Get the dimensions from a scaled URL
	@param url the url.
	@param defaults optional default dimension.
	@return the dimensions.
--]
[#function getDim url defaults=[215,125]]
	[#local res=url?matches("w(\\d+)h(\\d+)")]
	[#if res?has_content]
		[#local m=res[0]]
		[#return [m?groups[1]?number, m?groups[2]?number]]
	[/#if]
	[#return defaults]
[/#function]

[#--
	Figure out the file size and return a friendly string
	@param size An integer in bytes of how big the file is.
	@return the friendly string.
--]
[#function friendlyFileSize size]
	[#if size < 1024]
		[#return size + " b"]
	[/#if]
	[#if size < 1048576]
		[#return (size/1024)?round + " kB"]
	[/#if]
	[#if size < 1073741824]
		[#return (size/1048576)?string("0.#") + " MB"]
	[/#if]
	[#return size]
	[#-- > Freemarker isn't supported in syntax highlighting so need this close bracket --]
[/#function]

[#--
	Parses an ISO 8601 duration date and returns a friendly representation.  (Not full implementation)
	@param time Duration string
	@return A friendly formatted string representation of the time
--]
[#function friendlyDuration time]
	[#local ret = time]
	[#local result = time?matches("^(?:P)([^T]*)(?:T)?((\\d*)H)*((\\d*)M)*(([\\d\\.]*)S)*$")]

	[#if result]
		[#local hours = parseNumber(result?groups[3], -1)]
		[#local minutes = parseNumber(result?groups[5], -1)]
		[#local seconds = parseNumber(result?groups[7], -1)]

		[#local ret]
			[@compress single_line=true]
				[#if hours > -1]${hours} Hour[#if hours != 1]s[/#if][/#if]
				[#if minutes > -1]${minutes} Minute[#if minutes != 1]s[/#if][/#if]
				[#if seconds > -1]${seconds} Second[#if seconds != 1]s[/#if][/#if]
			[/@compress]
		[/#local]
	[/#if]

	[#return ret]
[/#function]

[#--
	Takes a URL and outputs the URL with http protocol if it is missing
	@param url URL to check
	@return A link including the protocol
--]
[#function URL url]
	[#local ret = (url)!""]
	[#if !url?matches('^(http:\\/\\/|https:\\/\\/)')?has_content]
		[#local ret = 'http://' + ret]
	[/#if]
	[#return ret]
[/#function]

[#--
	Takes a piece of text and strips out characters to make a better URL text slug
 @param text Text you want to convert to a URL safe sluf
 @return ret slug text
--]
[#function urlText text]
[#return text?trim?lower_case?replace("[^a-zA-Z0-9\\-_]", '-', 'ri')?replace('-+', '-', 'r')?replace('^-', '', 'ri')?replace('-$', '', 'ri')]
[/#function]


[#--
 Takes a needle and haystack to highlight text in a given string
 @param haystack Text to check for keyword highlight
 @param needle Text to highlight 
--]
[#macro highlightText haystack needle]
[@compress single_line=true]
[#if !needle?has_content]
	${haystack}
[#else]
	${haystack?replace('('+needle+')', '<span class="hl">$1</span>', 'ri')}
[/#if]
[/@compress]
[/#macro]

[#--
 Takes a string path and removes the protocol, query params, and jsession id
 @param path URL path
 @return A stripped down string of the URL for comparisons of page paths
--]
[#function linkStripped path]
	[#local path = stripJsession(path)]
	[#local path = path?replace('(http|https)://', '//', 'ri')]
	[#local path = path?replace('/FdxbQVIpg.*$', '', 'ri')]
	[#local path = path?split('?')]
	[#return path[0]]
[/#function]

[#--
 Takes a string path and removes the jsession id
 @param path URL path
 @return the path you passed in, sans jsession
--]
[#function stripJsession path]
	[#return path?replace(';jsessionid=[a-z0-9\\.\\-_]+', '', 'ri')]
[/#function]

[#--
	Takes an ID and text and creates a slug
	@param id ID of the record
	@param text Any descriptive text for the URL, most likely a title
	@return slug for URL
--]
[#function makeSlug id text]
	[#return id?c + '-' + urlText(text)]
[/#function]

[#--
	Takes the request.path_info URL, and returns the slug
 @param default A string to return if we didn't get anything
 @return ret a string of the current slug
--]
[#function getSlug default=""]
	[#local ret = default]
	[#if request.path_wildcard && request.path_info??]
		[#local result = stripJsession(request.path_info)?matches("^/?(.*)$")]
		[#if result]
			[#local ret = result?groups[1]]
		[/#if]
		[#if ret == ""]
			[#local ret = default]
		[/#if]
	[/#if]
	[#return ret]
[/#function]

[#--
	Truncate text with a string truncation text if it exceeds max
	@param str String to truncate
	@param length Max length of the string
	@param suffix OPTIONAL - Text to use if string exceeds max length
	@return truncated string
--]
[#function truncate str length suffix="..."]
	[#local ret = str]
	[#if ret?length > length]
		[#local ret = ret?substring(0, length) + suffix]
	[/#if]
	[#return ret]
[/#function]

[#--
	Get the ID from a slug
	@param slug OPTIONAL - Slug text
	@param default OPTIONAL - Default ID if it can't find one
	@return id from the slug
--]
[#function getIdFromSlug slug="" default="-1"]
	[#if !slug?has_content]
		[#local slug = getSlug()]
	[/#if]
	[#local ret = default]
	[#local result = slug?matches("^([\\d]+)\\-?.*$")]
	[#if result]
		[#local ret = result?groups[1]]
	[/#if]
	[#return ret?number]
[/#function]


[#--
Generate a external URL, mainly used for social media sharing of links
@param hostname Hostname of the site for full path
@param link Base link for the URL
@param path_info OPTIONAL - URL Path info
@param secure OPTIONAL - boolean on whether link needs to be secure protocol
@return url URL of the parts supplied that is url encoded
--]
[#function externalUrlGen hostname link path_info="" secure=false]
	[#local link_str = (stripJsession(link.url.setPathInfo(path_info).getURL()))!""]
	[#local protocol = 'http://']
	[#if secure]
		[#local protocol = 'https://']
	[/#if]
	[#return (protocol + hostname + link_str)!""]
[/#function]

[#--
	Takes a string number and tries to convert it to a number if it can
	NOTE: you cannot include commas or any kind of group separators.  Only a single period is allowed.
	@param number Number to parse
	@param default OPTIONAL - default returned if it fails, (0)
	@return a number.
--]
[#function parseNumber number default=0]
	[#--
		test cases:
	<div>${parseNumber("0.2")}</div>
	<div>${parseNumber("015.22")}</div>
	<div>${parseNumber("12330.")}</div>
	<div>${parseNumber("12220")}</div>
	<div>${parseNumber("15.21")}</div>
	<div>${parseNumber(".21")}</div>
	<div>${parseNumber("-0.2")}</div>
	<div>${parseNumber("-015.22")}</div>
	<div>${parseNumber("-12330.")}</div>
	<div>${parseNumber("-12220")}</div>
	<div>${parseNumber("-15.21")}</div>
	<div>${parseNumber("-.21")}</div>
	<div>${parseNumber("+1E10")}</div>
	<div>${parseNumber("1.E-4")}</div>
	<div>${parseNumber(".123E10")}</div>
	<div>${parseNumber("6.5E-5")}</div>
	<div>${parseNumber("-1E10")}</div>
	<div>${parseNumber("-1.E-4")}</div>
	<div>${parseNumber("-.123E10")}</div>
	<div>${parseNumber("-6.5E-5")}</div>
	--]
	[#if number?trim?matches("^[+\\-]?((\\d+\\.?\\d*)|(\\d*\\.?\\d+))(E[+\\-]?\\d+)?$")]
		[#return number?trim?number]
	[/#if]
	[#return default]
[/#function]

[#--
Format money 
@param amount Money amount to format
@param includeCents OPTIONAL - whether to include cents (don't round)
@return displayAmount Formatted string
--]
[#function formatCurrency amount includeCents=false]
	[#local displayAmount = ""]
	[#if amount == 0]
		[#local displayAmount = "$0"]
	[#else]
		[#if includeCents]
			[#local displayAmount = "$" + amount?string('###,###,##0.00')]
		[#else]
			[#local displayAmount = "$" + amount?floor?string('###,###,##0')]
		[/#if]
	[/#if]
	[#return displayAmount]
[/#function]

[#--
Create paging based on limit, offset, and count
@param os Offset 
@param limit Limit of posts per page
@param count Count of the number of posts
@param params OPTIONAL - parameters to include in the URLs
--]
[#macro pager os limit count params={}]
	<div class="paging">
		<div class="pager">
		[#local p_os = os]
		[#local p_limit = limit]
		[#local p_start = p_os + 1]
		[#local p_end = p_os + p_limit]

		[#local p_total = count]

		[#if p_end > p_total]
			[#local p_end = p_total]
		[/#if]

		[#local url_params = params]

		[#local page_total = (p_total / p_limit)?ceiling]
		[#local page_cur = (p_os / p_limit)?floor]

		[#local page_show_limit = 9]
		[#local page_show_sides = 4]
		[#local show_prev_edge = false]
		[#local show_next_edge = false]

		[#local page_main_start = 0]
		[#if page_cur > (page_show_limit - page_show_sides - 1)]
			[#local page_main_start = (page_cur - page_show_sides)]
			[#local show_prev_edge = true]
		[/#if]

		[#local page_main_end = (page_cur + page_show_sides)]
		[#if page_total < (page_main_end  + page_show_sides - 1)][#-- > fix codemirror display --]
			[#local page_main_start = (page_total - page_show_limit)]
			[#local page_main_end = (page_total - 1)]
		[#else]
			[#local page_main_end = (page_main_start + page_show_limit - 1)]
			[#local show_next_edge = true]
		[/#if]

		[#if page_total > 1]
			[#if page_cur != 0]
			<span class="backwards controls">
				<span class="prev control"><a class="val"
					href="${links.getCMSLink('cms:/pg/-1').url.addParams(url_params).addParam('os', ((page_cur-1)*p_limit)).getURL()}">
					Prev</a></span>
			</span>
			[/#if]
			[#if show_prev_edge]<span class="edge_sep prev_edge_sep"><span class="val">...</span>[/#if]
			<span class="pages">
				[#if page_total > page_show_limit]
					[#list page_main_start..page_main_end as temp]
						[#if page_cur == temp]
							<span class="page cur[#if temp_index == 0] first[/#if][#if !temp_has_next] last[/#if]">
								<span class="val">${(temp+1)}</span></span>
						[#else]
							<span class="page control[#if temp_index == 0] first[/#if][#if !temp_has_next] last[/#if]">
								<a class="val" href="${links.getCMSLink('cms:/pg/-1').url.addParams(url_params).addParam('os', (temp*p_limit))}">
									<span>${(temp+1)}</span></a></span>
						[/#if]
					[/#list]
				[#else]
					[#list 0..(page_total-1) as temp]
						[#if page_cur == temp]
							<span class="page cur[#if temp_index == 0] first[/#if][#if !temp_has_next] last[/#if]">
								<span class="val">${(temp+1)}</span></span>
						[#else]
							<span class="page control[#if temp_index == 0] first[/#if][#if !temp_has_next] last[/#if]">
								<a class="val" href="${links.getCMSLink('cms:/pg/-1').url.addParams(url_params).addParam('os', (temp*p_limit))}">
									<span>${(temp+1)}</span></a></span>
						[/#if]
					[/#list]
				[/#if]
			</span>
			[#if show_next_edge]<span class="edge_sep next_edge_sep"><span class="val">...</span></span>[/#if]
			[#if (page_cur+1) != page_total]
			<span class="forwards controls">
				<span class="next control"><a class="val"
						href="${links.getCMSLink('cms:/pg/-1').url.addParams(url_params).addParam('os', ((page_cur+1)*p_limit))}">Next</a>
				</span>
			</span>
			[/#if]
		[/#if]
		</div>
	</div>
[/#macro]



[#--
Calculate the percent while making sure it doesn't exceed 100%
@param amount Amount 
@param total Total amount
@param clamp_100 Whether to make the percent max at out 100
@return A whole number that represents the percentage
--]
[#function totalPercent amount total clamp_100=true]  
	[#local amount = (amount)!0]
	[#local total = (total)!0]
	[#if total == 0]
		[#local ret = 0]
	[#else]
		[#local ret = ((amount / total) * 100)?round]
		[#if clamp_100 && ret > 100]
			[#local ret = 100]
		[/#if]
	[/#if]
	[#return ret]
[/#function]



[#--
Get the HTML of a scaled image and make sure it uses either the original image or scaled depending on size
@param image Image to scale
@param width Max width of the image
@param height Max height of the image
@param alt OPTIONAL - Alternate text for the image
@param force_dims OPTIONAL - Boolean on whether to force image dimensions to size (could break aspect ratio)
@param vertical_center OPTIONAL - Boolean on whether to force the image to have a margin offset based
 on the actual height vs the max height
@param include_itemprop OPTIONAL - Boolean on whether to add an itemprop attribute to the image
@return Return the HTML for a scaled image     
--]          
[#function getScaledImage image width height alt="" force_dims=false vertical_center=false include_itemprop=true]
	[#local image_html = ""]
	[#local image_src = (image.url)!'']
	[#if image_src?has_content]
		[#local image_w = (image.width)!0]
		[#local image_h = (image.height)!0]
		[#--
			Scale if, width is larger, height is larger, or width and height is larger than the requested height.
		--]
		[#if (image_w > width && width > 0) || (image_h > height && height > 0) || (image_w > width && image_h > height)]
			[#local image_src = (image.getScaledURL(width, height))!""]
			[#local image_dims = getDim(image_src, [image_w, image_h])]
			[#local image_w = image_dims[0]]
			[#local image_h = image_dims[1]]
		[/#if]
		[#if force_dims]
			[#local image_w = width]
			[#local image_h = height]
		[/#if]
		[#local style = ""]
		[#if vertical_center]
			[#local margin_offset = (height - image_h) / 2]
			[#local style = 'style="margin-top: ${margin_offset?c}px;"']
		[/#if]
		[#if image_w == 0][#local image_w = ""][#else][#local image_w = image_w?c][/#if]
		[#if image_h == 0][#local image_h = ""][#else][#local image_h = image_h?c][/#if]
	[#local itemprop_html = ""]
	[#if include_itemprop]
		[#local itemprop_html = ' itemprop="image"']
	[/#if]
		[#local image_html = '<img ' + itemprop_html + ' ${style} src="${image_src}"
			alt="${alt?html}" width="${image_w}" height="${image_h}" />']
	[/#if]
	[#return image_html]
[/#function]

[#--
Get the HTML of a image and make sure it will display based on whether it exists
@param image Image to scale
@param alt OPTIONAL - Alternate text for the image
@return Return the HTML for a image     
--]          
[#function getImage image alt=""]
	[#local image_html = ""]
	[#local image_src = (image.url)!'']
	[#if image_src?has_content]
		[#local image_w = (image.width)!0]
		[#local image_h = (image.height)!0]
		[#if image_w == 0][#local image_w = ""][#else][#local image_w = image_w?c][/#if]
		[#if image_h == 0][#local image_h = ""][#else][#local image_h = image_h?c][/#if]
			[#local image_html = '<img itemprop="image" src="${image_src}" alt="${alt?html}" width="${image_w}" height="${image_h}" />']
	[/#if]
	[#return image_html]
[/#function]

[#--
Get the src location of a scaled image and make sure it uses either the original image or scaled depending on size
@param image Image to scale
@param width Max width of the image
@param height Max height of the image
@param external OPTIONAL - whether to include full path to image
@param protocol OPTIONAL - URL protocol for external path
@return Return the path for a scaled image     
--]
[#function getScaledImageSrc image width height external=false protocol="//"]
	[#local image_src = (image.url)!'']
	[#if image_src?has_content]
		[#local image_w = (image.width)!0]
		[#local image_h = (image.height)!0]
		[#if (image_w > width && width > 0) || (image_h > height && height > 0) || (image_w > width && image_h > height)]
			[#local image_src = (image.getScaledURL(width,height))!""]
		[/#if]
	[/#if]
	[#if external]
		[#local hostname = (hostname)!""]
		[#local image_src = protocol + hostname + image_src]
	[/#if]
	[#return image_src]
[/#function]


[#--
Check if image exists, if not use the provided fallback
@param image Image to test
@param fallback Image to use if the other doesn't exist
@return Return the path for a image     
--]
[#function imageFallback image fallback]
	[#local image_src = (image.url)!'']
	[#if image_src?has_content]
		[#return image]
	[/#if]
	[#return fallback]
[/#function]
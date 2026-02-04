(function ($) {
    $(document).ready(function () {
        var $infoIcon = $('#unavailablePopoverContentElement');
        var $hiddenContent = $('#UnavailableAccountPopover');

        if ($ && typeof $.fn.popover === 'function' && $infoIcon.length && $hiddenContent.length) {

            // We pull the HTML content first to avoid the e.hasOwnProperty bug
            // triggered by certain internal Bootstrap option merging.
            var popoverHtml = $hiddenContent.html();

            $infoIcon.popover({
                html: true,
                container: 'body',
                placement: 'right',
                trigger: 'click',
                content: popoverHtml // Pass the pre-retrieved HTML string
            });

            $(document).on('click', function (e) {
                if (!$infoIcon.is(e.target) && $infoIcon.has(e.target).length === 0 && $('.popover').has(e.target).length === 0) {
                    $infoIcon.popover('hide');
                }
            });
        }
    });
})(window.jQuery);
(function ($) {
    $(document).ready(function () {
        var $infoIcon = $('#unavailablePopoverContentElement');
        var $hiddenContent = $('#UnavailableAccountPopover');

        if ($ && typeof $.fn.popover === 'function' && $infoIcon.length && $hiddenContent.length) {

            var popoverHtml = $hiddenContent.html();

            $infoIcon.popover({
                html: true,
                container: 'body',
                placement: 'right',
                trigger: 'click',
                content: popoverHtml
            });

            $(document).on('click', function (e) {
                if (!$infoIcon.is(e.target) &&
                $infoIcon.has(e.target).length === 0 && $('.popover').has(e.target).length === 0) {
                    $infoIcon.popover('hide');
                }
            });
        }
    });
})(window.jQuery);
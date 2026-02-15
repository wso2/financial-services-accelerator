(function ($) {
    $(document).ready(function () {
        var $infoIcons = $('.unavailable-popover-content-element');

        if ($ && typeof $.fn.popover === 'function' && $infoIcons.length) {
            $infoIcons.each(function () {
                var $infoIcon = $(this);
                var $hiddenContent = $infoIcon.closest('.pb-1').find('.unavailable-account-popover');

                if (!$hiddenContent.length) {
                    return;
                }

                $infoIcon.popover({
                    html: true,
                    container: 'body',
                    placement: 'right',
                    trigger: 'hover focus',
                    content: $hiddenContent.html()
                });
            });
        }
    });
})(window.jQuery);
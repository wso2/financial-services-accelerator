/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

(function ($) {
    if (typeof $ !== 'function') {
       return;
    }
    $(document).ready(function () {
        var getTooltipContent = function ($infoIcon) {
            var $tooltipWrapper = $infoIcon.closest('.fs-tooltip-wrapper, .pb-1, .fs-select-tooltip-container');

            if (!$tooltipWrapper.length) {
                return $();
            }

            return $tooltipWrapper.find('.fs-tooltip-content').first();
        };

        var initializePopover = function ($infoIcon) {
            var $hiddenContent = getTooltipContent($infoIcon);

            if (!$hiddenContent.length || !$hiddenContent.text().trim()) {
                return;
            }

            $infoIcon.popover({
                html: true,
                container: 'body',
                placement: 'right',
                trigger: 'hover focus',
                content: function () {
                    return $hiddenContent.html();
                }
            });
        };

        var updateAccountSelectTooltip = function ($accountSelect) {
            var $tooltipContainer = $accountSelect.siblings('.fs-select-tooltip-container');
            if (!$tooltipContainer.length) {
                return;
            }

            var $selectedOption = $accountSelect.find('option:selected');
            var tooltipText = $selectedOption.data('tooltipDescription') || '';
            var hasTooltipText = String(tooltipText).trim().length > 0;
            var $tooltipContent = $tooltipContainer.find('.fs-select-tooltip-content');
            var $tooltipTrigger = $tooltipContainer.find('.fs-select-tooltip-trigger');

            $tooltipContent.text(tooltipText);
            $tooltipTrigger.attr('title', $selectedOption.text());

            if (hasTooltipText) {
                if (!$tooltipTrigger.data('bs.popover')) {
                    initializePopover($tooltipTrigger);
                }
                $tooltipTrigger.removeClass('hide');
            } else {
                $tooltipTrigger.popover('hide');
                $tooltipTrigger.addClass('hide');
            }
        };

        var $infoIcons = $('.fs-tooltip-trigger');
        var $accountSelects = $('.fs-select-with-tooltip');

        if ($ && typeof $.fn.popover === 'function' && $infoIcons.length) {
            $infoIcons.each(function () {
                initializePopover($(this));
            });
        }

        if ($accountSelects.length) {
            $accountSelects.each(function () {
                updateAccountSelectTooltip($(this));
            });

            $accountSelects.on('change', function () {
                updateAccountSelectTooltip($(this));
            });
        }
    });
})(window.jQuery);

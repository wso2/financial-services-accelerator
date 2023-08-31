## Use the API
Follow the instructions given below to implement each use case.

### Configure

```js
import React from "react";
import { LocaleProvider, Dropdown, useLocale } from "@bfsi-react/i18n";

const config = {
  locales: [
    {
      code: "en",
      display: "English",
      direction: "ltr",
      messages: {
        "app.unique.locale.text": "random text",
      },
      isDefault: true,
    },
    {
      code: "en-lk",
      display: "සිංහල",
      direction: "rtl",
      messages: {
        "app.unique.locale.text": "අහඹු පෙළ",
      },
    },
  ],
};

function App() {
  const { getActiveLocale, changeLocale, supportedLocales } = useLocale(config);
  const activeLocale = getActiveLocale();

  return (
    <LocaleProvider locale={activeLocale}>
      <div className="App" dir={activeLocale.direction}>
        <Dropdown
          value={activeLocale.code}
          onChange={changeLocale}
          options={supportedLocales}
        />
      </div>
    </LocaleProvider>
  );
}

export default App;
```

## Access Locale Texts


```js
import React from "react";
import { FormattedMessage } from "react-intl";

export const Page = () => {
  return (
    <div>
      ...
      <FormattedMessage
        id="app.unique.locale.text"
        defaultMessage="default random text"
      />
      ...
    </div>
  );
};
```

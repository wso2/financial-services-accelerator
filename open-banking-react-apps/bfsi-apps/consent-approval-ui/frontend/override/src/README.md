# Override folder
If you want to override a certain React Component/File from [frontend/source/src](../../source/src/) folder of the 
default Consent Approval application, this is the correct place to do it. You should copy the required files which you 
are going to customize to the [frontend/override/src](../src) folder. You do not have to copy the entire directory, only 
copy the desired file/files.

#### Example
Following will override the AccountSelection component.
```sh
override
└── src
    ├── Readme.md
    └── pages
        └── consentPages
            └── AccountSelection.jsx

```
 
## Modify an existing component or style
Any file inside [frontend/override/src](../src) folder can override the original file at 
[frontend/source/src](../../source/src/) folder. The name of the file and location relative to the source folder has to 
be identical. In this method you can customize an existing component or scss style files.

## Add a new component to an existing page
You can add your own files to customize the UI in the [frontend/override/src](../src) folder. You also need to copy the 
file in which the new component will take place to the override folder.

Then you can import the **NewComponent.jsx** to the required file by adding the **AppOverride** prefix to the import 
and provide the full path relative to the override folder.

```import NewFile from 'AppOverride/src/components/NewComponent.jsx';```

## Add an entire new page
You can create and add a new page to the application. First add the new page file to the [frontend/override/src](../src) 
folder and import it to the [consent-approval-ui/configs.js](../../../configs.js) file.

Here you do not need to use the **AppOverride** prefix. Then you can add the new page to the **consentPages** array in 
the [consent-approval-ui/configs.js](../../../configs.js) file like below.

```
consentPages: [
    {
        index: 1,
        Component: AccountSelection
    },
    {
        index: 2,
        Component: consentType==="accounts"? AccountConsent:PaymentConsent
    },
    {
        index: 3,
        Component: ConfirmConsent
    },
    {
        index: 4,
        Component: NewPage
    }
]
```

## Specify the order of the pages
You can specify in which order the pages must be displayed by adding the relevant position of each page to the index 
property. For example if you want to display the NewPage before the ConfirmConsent page, you can configure by changing 
the index property of each array element as below. The order of the consentPages will be in the ascending order of the
index property. The component with the lower index will be rendered first.

```
consentPages: [
    {
        index: 1,
        Component: AccountSelection
    },
    {
        index: 2,
        Component: consentType==="accounts"? AccountConsent:PaymentConsent
    },
    {
        index: 4,
        Component: ConfirmConsent
    },
    {
        index: 3,
        Component: NewPage
    }
]
```
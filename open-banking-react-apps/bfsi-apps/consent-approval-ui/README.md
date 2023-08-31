# consent-approval-ui
Consent approval web application.

# Consent approval application structure

**frontend/source folder**: Includes the code for the default Consent approval application.

**frontend/override folder**: If you want to override a certain React Component/File from 
[frontend/source/src](frontend/source/src) folder of the default Consent Approval application use this folder. You 
should copy the required files which you are going to customize to the [frontend/override/src](frontend/override/src) 
folder. You do not have to copy the entire directory, only copy the desired file/files. If you want to add any new 
component or page to the flow, add it to this folder in the correct path. Refer to the Readme at override folder for 
further information.

**webpack.config.js**: Contains webpack configurations.

**configs.js**: Configure the pages and their order to be displayed in the application.

**loader.js**: Custom webpack loader to handle default components being overridden by custom components.

## Start up in Development mode

1. Navigate to the [consent-approval-ui](../consent-approval-ui) directory in the terminal and run the 
following command.

>npm install

2. To load the React app in development mode, run the following command in the
[consent-approval-ui](../consent-approval-ui) directory.

>npm start

3. Set the 'USE_DEFAULT_CONFIGS' constant in [frontend/source/src/config.js](frontend/source/src/config.js) to false 
and enter the origin of IS server for the serverUrl value.


4. Access the deployed application in the flow and extract the path of the AccountSelection page. Append it to the 
localhost origin and enter that URL in another tab.


5. To see the changes you made in the deployed app, run the following command in the 
[consent-approval-ui](../consent-approval-ui) directory.

>npm run build

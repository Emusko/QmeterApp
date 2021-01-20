# PayDayChallenge
Challenge
1. I couldn't develop any test on the application. 
Since, I have not such a good experience on the test I thought the good 
side of application will not suit the testing codes.

At first Ito be honest, I couldn't expect you would send me the code challenge.
I didn't have a much time to do it. The project built in shorter than one day.
So, if I could have a good time, I would write comments, create little methods to make
the project understandable and easy to read, store user details in sharedPreferences for sorting
accounts, transactions after authenticating. Also, it could have been good for opening activities
from the deeplink, so I would create deeplinks. 
Filtering and attaching to the recyclerView (In the MonthlyExpenseActivity) has not been good as I thought
I would make some difference to make it better for each filter command. 
Sensitive data (base Url) stored on the build.gradle(:app) file, 
but I would search and find the new best experiences for doing it. 
In additionally, I would make many branches for the setting up, creating new features and other things like that
for seperating the development phases. It is good for if there was a mistake between feature changes it will be easily detected.
And branches all will be together at 'develop' branch before go to production.

2. The Most useful feature is of course Dagger2 :) it is my friend in almost every class
Injecting the provided classes and avoiding dependency gave me a good boost.

3. Play Console is enough to track the performance. I used it in Portmanat application and
crashes downed to 2%. Because reports submitted every day and I was checking all.

4. If there were some issue related to the API, then I print stack trace from the Rx Throwable
After the project completed I deleted all printing stack traces. Throwable doesn't show the only server side errors
I have to mention about it. But it is comfortable for me. If there is an issue about API I would see from Throwable.

5. I would suggest this server for adding error messages, codes or something like that. 
Response code to know response is ok or not. Not to use "First Name" as a property name. 
Use token for the sessions. Give accounts and transactions especailly for the user id with sending parameter.

using System;
using System.Threading.Tasks;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.SceneManagement;

public class LoginHandler : MonoBehaviour {

    public GameObject initialsObject;
    InputField initialsField;

    public GameObject passgameobj;
    InputField passinpfld;


    public GameObject tbemail;
    public GameObject tbpwd;
    private string email;
    private string pwd;

    protected Firebase.Auth.FirebaseAuth auth;
    private Firebase.Auth.FirebaseAuth otherAuth;
    protected Dictionary<string, Firebase.Auth.FirebaseUser> userByAuth =
      new Dictionary<string, Firebase.Auth.FirebaseUser>();
    Firebase.DependencyStatus dependencyStatus = Firebase.DependencyStatus.UnavailableOther;
    void Awake()
    {
        tbemail = GetComponent<GameObject>();
        tbpwd = GetComponent<GameObject>();
    }
    private Firebase.AppOptions otherAuthOptions = new Firebase.AppOptions
    {
        ApiKey = "",
        AppId = "",
        ProjectId = ""
    };

    // Use this for initialization
    public virtual void Start () {

        initialsField = initialsObject.GetComponent<InputField>();
        passinpfld = passgameobj.GetComponent<InputField>();


        tbemail = (GameObject)GetComponent<GameObject>();
        tbpwd = GetComponent<GameObject>();

        Firebase.FirebaseApp.CheckAndFixDependenciesAsync().ContinueWith(task => {
            dependencyStatus = task.Result;
            if (dependencyStatus == Firebase.DependencyStatus.Available)
            {
                InitializeFirebase();
            }
            else
            {
                Debug.LogError(
                  "Could not resolve all Firebase dependencies: " + dependencyStatus);
            }
        });

    }

    void InitializeFirebase()
    {
        //DebugLog("Setting up Firebase Auth");
        auth = Firebase.Auth.FirebaseAuth.DefaultInstance;
        auth.StateChanged += AuthStateChanged;
        auth.IdTokenChanged += IdTokenChanged;
        // Specify valid options to construct a secondary authentication object.
        if (otherAuthOptions != null &&
            !(String.IsNullOrEmpty(otherAuthOptions.ApiKey) ||
              String.IsNullOrEmpty(otherAuthOptions.AppId) ||
              String.IsNullOrEmpty(otherAuthOptions.ProjectId)))
        {
            try
            {
                otherAuth = Firebase.Auth.FirebaseAuth.GetAuth(Firebase.FirebaseApp.Create(
                  otherAuthOptions, "Secondary"));
                otherAuth.StateChanged += AuthStateChanged;
                otherAuth.IdTokenChanged += IdTokenChanged;
            }
            catch (Exception)
            {
                DebugLog("ERROR: Failed to initialize secondary authentication object.");
            }
        }
        AuthStateChanged(this, null);
    }


    public Task SigninAsync()
    {
        Debug.Log(initialsField.text);

        email = initialsField.text;// "vijay@outlook.com";
        pwd = passinpfld.text; // "titanic";
        DebugLog(String.Format("Attempting to sign in as {0}...", email));

        DisableUI();
        return auth.SignInWithEmailAndPasswordAsync(email, pwd)
          .ContinueWith(HandleSigninResult);
    }


    void OnDestroy()
    {
        auth.StateChanged -= AuthStateChanged;
        auth.IdTokenChanged -= IdTokenChanged;
        auth = null;
        if (otherAuth != null)
        {
            otherAuth.StateChanged -= AuthStateChanged;
            otherAuth.IdTokenChanged -= IdTokenChanged;
            otherAuth = null;
        }
    }
    bool UIEnabled = true;

    void DisableUI()
    {
        UIEnabled = false;
    }

    void EnableUI()
    {
        UIEnabled = true;
    }
    protected string displayName = "";

    // Track state changes of the auth object.
    void AuthStateChanged(object sender, System.EventArgs eventArgs)
    {
        Firebase.Auth.FirebaseAuth senderAuth = sender as Firebase.Auth.FirebaseAuth;
        Firebase.Auth.FirebaseUser user = null;
        if (senderAuth != null) userByAuth.TryGetValue(senderAuth.App.Name, out user);
        if (senderAuth == auth && senderAuth.CurrentUser != user)
        {
            bool signedIn = user != senderAuth.CurrentUser && senderAuth.CurrentUser != null;
            if (!signedIn && user != null)
            {
                DebugLog("Signed out " + user.UserId);
            }
            user = senderAuth.CurrentUser;
            userByAuth[senderAuth.App.Name] = user;
            if (signedIn)
            {
                DebugLog("Signed in " + user.UserId);
                displayName = user.DisplayName ?? "";
                DisplayDetailedUserInfo(user, 1);

                SceneManager.LoadScene("Welcome");
            }
        }
    }
    void DisplayUserInfo(Firebase.Auth.IUserInfo userInfo, int indentLevel)
    {
        string indent = new String(' ', indentLevel * 2);
        var userProperties = new Dictionary<string, string> {
      {"Display Name", userInfo.DisplayName},
      {"Email", userInfo.Email},
      {"Photo URL", userInfo.PhotoUrl != null ? userInfo.PhotoUrl.ToString() : null},
      {"Provider ID", userInfo.ProviderId},
      {"User ID", userInfo.UserId}
    };
        foreach (var property in userProperties)
        {
            if (!String.IsNullOrEmpty(property.Value))
            {
                DebugLog(String.Format("{0}{1}: {2}", indent, property.Key, property.Value));
            }
        }
    }


    void DisplayDetailedUserInfo(Firebase.Auth.FirebaseUser user, int indentLevel)
    {
        DisplayUserInfo(user, indentLevel);
        DebugLog("  Anonymous: " + user.IsAnonymous);
        DebugLog("  Email Verified: " + user.IsEmailVerified);
        var providerDataList = new List<Firebase.Auth.IUserInfo>(user.ProviderData);
        if (providerDataList.Count > 0)
        {
            DebugLog("  Provider Data:");
            foreach (var providerData in user.ProviderData)
            {
                DisplayUserInfo(providerData, indentLevel + 1);
            }
        }
    }

    // Track ID token changes.
    void IdTokenChanged(object sender, System.EventArgs eventArgs)
    {
        Firebase.Auth.FirebaseAuth senderAuth = sender as Firebase.Auth.FirebaseAuth;
        if (senderAuth == auth && senderAuth.CurrentUser != null && !fetchingToken)
        {
            senderAuth.CurrentUser.TokenAsync(false).ContinueWith(
              task => DebugLog(String.Format("Token[0:8] = {0}", task.Result.Substring(0, 8))));
        }
    }
    private bool fetchingToken = false;
    // Log the result of the specified task, returning true if the task
    // completed successfully, false otherwise.
    bool LogTaskCompletion(Task task, string operation)
    {
        bool complete = false;
        if (task.IsCanceled)
        {
            DebugLog(operation + " canceled.");
        }
        else if (task.IsFaulted)
        {
            DebugLog(operation + " encounted an error.");
            foreach (Exception exception in task.Exception.Flatten().InnerExceptions)
            {
                string authErrorCode = "";
                Firebase.FirebaseException firebaseEx = exception as Firebase.FirebaseException;
                if (firebaseEx != null)
                {
                    authErrorCode = String.Format("AuthError.{0}: ",
                      ((Firebase.Auth.AuthError)firebaseEx.ErrorCode).ToString());
                }
                DebugLog(authErrorCode + exception.ToString());
            }
        }
        else if (task.IsCompleted)
        {
            DebugLog(operation + " completed");
            complete = true;
        }
        return complete;
    }

    // This is functionally equivalent to the Signin() function.  However, it
    // illustrates the use of Credentials, which can be aquired from many
    // different sources of authentication.
    public Task SigninWithCredentialAsync()
    {
        DebugLog(String.Format("Attempting to sign in as {0}...", email));
        DisableUI();
        Firebase.Auth.Credential cred = Firebase.Auth.EmailAuthProvider.GetCredential(email, pwd);
        return auth.SignInWithCredentialAsync(cred).ContinueWith(HandleSigninResult);
    }
    void HandleSigninResult(Task<Firebase.Auth.FirebaseUser> authTask)
    {
        EnableUI();
        LogTaskCompletion(authTask, "Sign-in");
    }
    // Output text to the debug log text field, as well as the console.
    public void DebugLog(string s)
    {
        Debug.Log(s);
        //logText += s + "\n";

        //while (logText.Length > kMaxLogSize)
        //{
        //    int index = logText.IndexOf("\n");
        //    logText = logText.Substring(index + 1);
        //}
        //scrollViewVector.y = int.MaxValue;
    }

    public void Go()
    {
        SigninAsync();

    }


    // Update is called once per frame
    void Update () {
        if (tbemail != null)
        {
            email = tbemail.GetComponent<InputField>().text;
            if (tbpwd != null)
            {
                pwd = tbpwd.GetComponent<InputField>().text;
            }
        }
    }
}

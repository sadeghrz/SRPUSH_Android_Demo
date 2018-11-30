package ir.srpush.androiddemo.srpushdemo.AsyncReqTools;

public class AsyncTaskManager extends android.os.AsyncTask<AsyncRequestParams,
        String, AsyncRequestParams> {

    public static void Start(AsyncRequestParams param) {

        AsyncTaskManager task = new AsyncTaskManager();
        task.execute(param);
    }

    @Override
    protected AsyncRequestParams doInBackground(AsyncRequestParams... params) {

        AsyncRequestParams result = params[0];
        result.set_Result(
                HttpManager.getData(
                        params[0].get_uri(),
                        params[0].get_Jobj(),
                        params[0].get_username(),
                        params[0].get_pass()
                )
        );
        return result;
    }

    @Override
    protected void onPostExecute(AsyncRequestParams s) {
        ((IRequestReciever) s.get_context()).ReqReciever(s.get_Result());
    }
}

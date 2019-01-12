package net.piotrturski.shop.order;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;

public class Util {

    /**
     * return result from the thread that actually contains response. handles sync and async responses
     * @param mvcResultFromControllerThread - MvcResult from mvc.perform
     * @return MvcResult with response
     */
    static public MvcResult getMvcResultWithResponse(MockMvc mvc, MvcResult mvcResultFromControllerThread) {
        try {
            return mvcResultFromControllerThread.getRequest().isAsyncStarted() ?
                    mvc.perform(asyncDispatch(mvcResultFromControllerThread)).andReturn()
                    : mvcResultFromControllerThread;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

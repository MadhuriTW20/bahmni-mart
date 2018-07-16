package org.bahmni.mart.notification;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(MailSender.class)
@RunWith(PowerMockRunner.class)
public class MailSenderTest {

    private MailSender mailSender;
    private Runtime runtime;
    private String sendMailCommand;
    private Logger logger;

    @Before
    public void setUp() throws Exception {
        mailSender = new MailSender();
        mockStatic(Runtime.class);
        runtime = Runtime.getRuntime();
        when(Runtime.getRuntime()).thenReturn(runtime);
        String body = "\"Subject: Notification regarding jobs\nFrom: abc@gmail.com\n" +
                "These following jobs failed during execution -\nobs\norders\n\"";
        String recipients = "recipientOne@gmail.com, recipientTwo@gmail.com";
        sendMailCommand = String.format("echo %s | sendmail -v %s", body, recipients);
        logger = mock(Logger.class);

        setValueForFinalStaticField(MailSender.class, "logger", logger);
        setValuesForMemberFields(mailSender, "subject", "Notification regarding jobs");
        setValuesForMemberFields(mailSender, "from", "abc@gmail.com");
        setValuesForMemberFields(mailSender, "recipients", recipients);
    }

    @Ignore
    @Test
    public void shouldLogErrorWhileExecutingSendmailCommand() throws Exception {
        List<String> namesOfJobs = Arrays.asList("obs", "orders");

        mailSender.sendMail(namesOfJobs);

        verify(logger).info("Can't send the mail for following failed jobs\nobs\norders");
    }

    @Test
    public void shouldExecutesSendMailCommandForGivenJobs() throws Exception {
        List<String> namesOfJobs = Arrays.asList("obs", "orders");

        mailSender.sendMail(namesOfJobs);

        verifyStatic();
        runtime.exec(new String[]{"bash", "-c", sendMailCommand});
    }


    @Test
    public void shouldNotExecuteSendMailCommandWhenNoJobsFailed() throws Exception {
        List<String> namesOfJobs = emptyList();

        mailSender.sendMail(namesOfJobs);

        verifyStatic(never());
        runtime.exec(new String[]{"bash", "-c", sendMailCommand});
    }

    @Test
    public void shouldNotExecuteSendMailCommandWhenRecipientsIsEmptyString() throws Exception {
        List<String> namesOfJobs = Arrays.asList("obs", "orders");
        String recipients = "";
        setValuesForMemberFields(mailSender, "recipients", recipients);

        mailSender.sendMail(namesOfJobs);

        verifyStatic(never());
        runtime.exec(any(String[].class));
    }

    @Test
    public void shouldNotExecuteSendMailCommandWhenFromAddressIsEmptyString() throws Exception {
        List<String> namesOfJobs = Arrays.asList("obs", "orders");
        String from = "";
        setValuesForMemberFields(mailSender, "from", from);

        mailSender.sendMail(namesOfJobs);

        verifyStatic(never());
        runtime.exec(any(String[].class));
    }

}



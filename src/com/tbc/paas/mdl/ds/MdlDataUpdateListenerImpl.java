package com.tbc.paas.mdl.ds;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.tbc.paas.mql.notify.MqlNotify;
import com.tbc.soa.json.JSONSerializer;

public class MdlDataUpdateListenerImpl extends Thread implements
		MdlDataUpdateListener {
	public static final String APP_CODE = "appCode";
	public static final String CORP_CODE = "corpCode";

	public static final Logger LOG = Logger
			.getLogger(MdlDataUpdateListenerImpl.class);

	private JSONSerializer serializer;
	private JmsTemplate template;
	private Destination destination;
	private BlockingDeque<List<MqlNotify>> dataUpdateMessageQueue;

	public MdlDataUpdateListenerImpl() {
		super();
		serializer = new JSONSerializer();
		dataUpdateMessageQueue = new LinkedBlockingDeque<List<MqlNotify>>();
	}

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public JmsTemplate getJmsTemplate() {
		return template;
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.template = jmsTemplate;
	}

	private MessageCreator getMessageCreator(
			final List<MqlNotify> dataUpdateInfo) {
		return new MessageCreator() {

			public Message createMessage(Session session) throws JMSException {
				TextMessage message = session.createTextMessage();
				String updateJsonData = serializer.deepSerialize(dataUpdateInfo);
				if (LOG.isInfoEnabled()) {
					LOG.info("Serializer JSON:" + updateJsonData);
				}
				message.setText(updateJsonData);
				return message;
			}
		};
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				List<MqlNotify> dataUpdateInfo = dataUpdateMessageQueue.take();
				this.template.send(destination,
						getMessageCreator(dataUpdateInfo));
			} catch (InterruptedException e) {
				int size = dataUpdateMessageQueue.size();
				if (size > 0) {
					LOG.warn("More than one data update info lost!");
				}
				break;
			}
		}
	}

	@Override
	public void updateNotify(List<MqlNotify> mqlTransUpdateNotify) {
		try {
			dataUpdateMessageQueue.put(mqlTransUpdateNotify);
		} catch (InterruptedException e) {
			LOG.warn("Can't put transation update data to dataUpdateMessageQueue");
		}
	}
}

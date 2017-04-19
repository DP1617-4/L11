
package services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import repositories.ChirpRepository;
import domain.Actor;
import domain.Chirp;
import domain.Chorbi;
import domain.Folder;
import domain.Urrl;

@Service
@Transactional
public class ChirpService {

	//Constructor

	public ChirpService() {
		super();
	}


	//Managed Repository

	@Autowired
	private ChirpRepository			messageRepository;

	//Auxiliary Services

	@Autowired
	private ActorService			actorService;

	@Autowired
	private ChorbiService			chorbiService;

	@Autowired
	private AdministratorService	adminService;

	@Autowired
	private FolderService			folderService;


	//CRUD

	public Chirp create() {
		final Chirp result = new Chirp();
		Chorbi sender;
		final Collection<Urrl> attachments = new ArrayList<Urrl>();
		sender = this.chorbiService.findByPrincipal();
		final Folder senderFolder = this.folderService.findSystemFolder(sender, "Sent");
		result.setFolder(senderFolder);
		result.setMoment(new Date());
		result.setSender(sender);
		result.setAttachments(attachments);
		return result;
	}

	public Chirp findOne(final int messageId) {
		Chirp result;

		result = this.messageRepository.findOne(messageId);

		return result;
	}

	public Collection<Chirp> findAll() {
		return this.messageRepository.findAll();
	}

	public Collection<Chirp> findAllByFolder(final int folderId) {
		Collection<Chirp> result;
		this.folderService.checkPrincipal(folderId);
		result = this.messageRepository.findByFolderId(folderId);
		return result;
	}

	public Collection<Chirp> findAllByFolderWithNoCheck(final int folderId) {
		Collection<Chirp> result;
		result = this.messageRepository.findByFolderId(folderId);
		return result;
	}

	//	public Chirp save(Chirp message){
	//		Chirp result;
	//		folderService.checkPrincipal(message.getFolder());
	//		result = messageRepository.save(message);
	//		return result;
	//	}

	public void delete(final Chirp message) {

		this.checkPrincipal(message);

		this.messageRepository.delete(message);

	}

	//Business methods

	public Chirp send(final Chirp message) {

		Chorbi recipient;
		Folder recipientFolder;
		Folder senderFolder;
		Chorbi sender;

		sender = this.chorbiService.findByPrincipal();
		recipient = (Chorbi) message.getRecipient();

		recipientFolder = this.folderService.findSystemFolder(recipient, "Received");
		senderFolder = this.folderService.findSystemFolder(sender, "Sent");

		message.setMoment(new Date(System.currentTimeMillis() - 1));
		message.setFolder(senderFolder);

		this.messageRepository.save(message);

		message.setFolder(recipientFolder);

		this.messageRepository.save(message);

		return message;
	}

	public void addAttachment(final Chirp chirp, final String attachment) {

		final Urrl url = new Urrl();
		url.setLink(attachment);

		if (chirp.getAttachments() == null) {
			final Collection<Urrl> attachments = new HashSet<Urrl>();
			attachments.add(url);
			chirp.setAttachments(attachments);
		} else
			chirp.getAttachments().add(url);

	}
	public Chirp move(final Chirp message, final Folder folder) {
		Chirp result;
		this.checkPrincipal(message);
		this.folderService.checkPrincipal(folder);
		message.setFolder(folder);
		result = this.messageRepository.save(message);
		return result;
	}

	public void flush() {
		this.messageRepository.flush();

	}

	public Chirp reply(final Chirp chirp) {
		final Chirp result;
		result = this.create();
		result.setSubject("Re: " + chirp.getSubject());
		result.setRecipient(chirp.getSender());

		return result;
	}

	public Chirp reSend(final Chirp chirp, final Chorbi chorbi) {

		Chirp result;
		result = this.create();
		result.setAttachments(chirp.getAttachments());
		result.setSubject(chirp.getSubject());
		result.setText(chirp.getText());
		result.setRecipient(chorbi);
		this.send(result);

		return result;
	}
	// Principal Checkers

	public void checkPrincipalSender(final Chirp message) {
		final Actor actor = this.actorService.findByPrincipal();
		Assert.isTrue(actor.getUserAccount().equals(message.getSender()));
	}

	public void checkPrincipalRecipient(final Chirp message) {
		final Actor actor = this.actorService.findByPrincipal();
		Assert.isTrue(actor.getUserAccount().equals(message.getRecipient()));
	}

	public void checkPrincipal(final Chirp message) {
		final Actor actor = this.actorService.findByPrincipal();

		Assert.isTrue(actor.equals(message.getSender()) || actor.equals(message.getRecipient()));
	}

}

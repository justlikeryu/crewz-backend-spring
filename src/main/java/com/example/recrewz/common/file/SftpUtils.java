package com.example.recrewz.common.file;

import com.example.recrewz.common.info.Info;
import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Component
public class SftpUtils {
//    private final JSch jSch = new JSch();
    private Session session;
    private ChannelSftp channelSftp;

    @Value("${spring.servlet.multipart.location}")
    private String path;

    /**
     * 서버와 연결에 필요한 값들을 가져와 초기화 시킴
     * @param host 서버 주소
     * @param userName 아이디
     * @param password 패스워드
     * @param port 포트번호
     * @param privateKey 개인키
     */
    public void init(String host, String userName, String password, int port, String privateKey) {
        JSch jSch = new JSch();
        Channel channel = null;
        try {
            if(privateKey != null) {//개인키가 존재한다면
                jSch.addIdentity(privateKey);
            }
            session = jSch.getSession(userName, host, port);

            if(privateKey == null && password != null) {//개인키가 없다면 패스워드로 접속
                session.setPassword(password);
            }

            // 프로퍼티 설정
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no"); // 접속 시 hostkeychecking 여부
            session.setConfig(config);
            session.setTimeout(3000);
            session.connect();

            //sftp로 접속
            channel = session.openChannel("sftp");
//            channel.connect();

            channelSftp = (ChannelSftp) channel;
            System.out.println("channelSftp: " + channelSftp.toString());
            channelSftp.connect();
        } catch (JSchException e) {
            throw new RuntimeException("Failed to establish SFTP connection", e);
        }
    }

    /**
     * 멤버 디렉토리 만들기
     * @param path 멤버 디렉토리 위치
     * @return 멤버 디렉토리 생성 여부
     */
    public boolean memberDir(String path) {
        boolean flag = false;

        try {
            channelSftp.mkdir(path);
            flag = true;
        } catch (SftpException e) {
            System.out.println("멤버 디렉토리 만들기 실패!");
        }

        return flag;
    }

    public boolean moveProfile(String dir, String name) {
        Channel c1 = null;
        Channel c2 = null;

        ChannelSftp upload = null;
        ChannelSftp download = null;

        try {
            c1 = session.openChannel("sftp");
            c2 = session.openChannel("sftp");

            c1.connect();
            c2.connect();

            upload = (ChannelSftp)c1;
            download = (ChannelSftp)c2;

            InputStream is = upload.get(Info.path + dir + File.separator + "default.png");
            download.put(is, Info.path + "member" + File.separator + name + File.separator + "default.png");
        } catch (JSchException | SftpException e) {
            throw new RuntimeException(e);
        }

        c1.disconnect();
        c2.disconnect();

        upload.disconnect();
        download.disconnect();

        return true;
    }

    /**
     * 디렉토리( or 파일) 존재 여부
     * @param path 디렉토리 (or 파일)
     * @return 파일 존재 여부
     */
    public boolean exists(String path) {
        Vector res = null;
        try {
            res = channelSftp.ls(path);
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }
        }
        return res != null && !res.isEmpty();
    }

    /**
     * 파일 업로드
     *
     * @param dir 저장할 디렉토리
     * @param file 저장할 파일
     * @return 업로드 여부
     */
    public synchronized boolean upload(String dir, MultipartFile file) {
        boolean isUpload = false;

        try {
            System.out.println("dir: " + dir);
            channelSftp.put(file.getInputStream(), dir + File.separator + file.getOriginalFilename());
            isUpload = true;
        } catch (SftpException | IOException e) {
            throw new RuntimeException(e);
        }

        return isUpload;
    }

    public synchronized boolean upload(int no, MultipartFile[] photos) {
        boolean isUpload = false;

        String dir = Info.path + "moim" + File.separator + no;
        try {
            channelSftp.mkdir(dir);
            channelSftp.cd(dir);

            for (MultipartFile photo : photos) {
                if(!photo.isEmpty()) {
                    channelSftp.put(photo.getInputStream(), photo.getOriginalFilename());
                    File file = new File(path + photo.getOriginalFilename());
                    photo.transferTo(file);
                    if(file.exists()) {
                        if(file.delete()) {
                            System.out.println("파일 삭제 완료!");
                            file = null;
                        }
                    }
                }
            }

            isUpload = true;
        } catch (SftpException | IOException e) {
            System.out.println(e.getMessage());
            System.out.println("모임 사진 업로드 실패!");
        }

        return isUpload;
    }

    public synchronized boolean deleteProfile(String dir, String photo) {
        boolean isUpload = false;

        try {
            channelSftp.rm(dir + File.separator + photo);

            isUpload = true;
        } catch (SftpException e) {
            System.out.println("delete 실패!");
        }

        return isUpload;
    }

    public synchronized byte[] downloadImg(String dir, String photo) {
        try {
//            if (channelSftp == null || !channelSftp.isConnected()) {
//                connection();
//            }
            synchronized (this) {
                channelSftp.cd(dir);
                System.out.println("dir:" + dir);
                System.out.println("photo: " + photo);
                try (InputStream in = channelSftp.get(photo)) {
//                if (in == null || in.read() == -1) {
//                    throw new RuntimeException("Failed to download image: InputStream is closed or empty");
//                }

//                return in.readAllBytes();
                    if (in == null) {
                        throw new RuntimeException("Failed to download image: InputStream is null");
                    }

                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;

                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }

                        byte[] imageData = out.toByteArray();
                        return imageData;
                    }
                }
            }
        } catch (SftpException | IOException e) {
            throw new RuntimeException("Failed to download image", e);
        }
    }

    public byte[] download(String dir, String photo) {
        InputStream in = null;
        byte[] buf = null;
        try {
            channelSftp.cd(dir);
            in = channelSftp.get(photo);
            System.out.println("in: " + in);
            buf = in.readAllBytes();
        } catch (SftpException | IOException e) {
            System.out.println("downloadImg: " + e.getCause());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        disconnection();

        return buf;
    }

    public boolean delete(String dir, String photo) {
        boolean isUpload = false;

        try {
            channelSftp.cd(dir);
            channelSftp.rm(photo);
            channelSftp.rmdir(dir);

            isUpload = true;
        } catch (SftpException e) {
            System.out.println("delete 실패!");
        }

        return isUpload;
    }

    public synchronized boolean deletePhoto(String dir, String photo) {
        boolean isUpload = false;

        try {
            channelSftp.cd(dir);
            channelSftp.rm(photo);

            isUpload = true;
        } catch (SftpException e) {
            System.out.println("사진 삭제 실패!");
        }

        return isUpload;
    }

    public boolean editImage(int no, String oldFile, MultipartFile mf) {
        boolean isUpload = false;

        try {
            channelSftp.cd(Info.path + "moim" + File.separator + no);

            if(oldFile != null)
                channelSftp.rm(oldFile);

            channelSftp.put(mf.getInputStream(), mf.getOriginalFilename());

            isUpload = true;
        } catch(SftpException | IOException e) {
            System.out.println(e.getMessage());
        }

        return isUpload;
    }

    public boolean mkdir(String path) {
        boolean flag = false;

        try {
            channelSftp.mkdir(path);
            flag = true;
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }

        return flag;
    }



    public synchronized boolean delDir(String path, String dir) {
        boolean flag = false;

        try {
            channelSftp.cd(path + "/" + dir);
            Vector<ChannelSftp.LsEntry> list = channelSftp.ls("*");
            for (ChannelSftp.LsEntry entry : list) {
                String fileName = entry.getFilename();
                if (!fileName.equals(".") && !fileName.equals("..")) {
                    System.out.println("Deleted file: " + fileName);
                    channelSftp.rm(fileName);
                }
            }
            channelSftp.rmdir(path + "/" + dir);
            flag = true;
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }

        return flag;
    }

    public ArrayList<String> listFiles(String dir) throws SftpException {
        @SuppressWarnings("unchecked")
        Vector<ChannelSftp.LsEntry> files = channelSftp.ls(dir);

        ArrayList<String> list = new ArrayList<>();

        for (ChannelSftp.LsEntry entry : files) {
            if (!entry.getAttrs().isDir()) {
                // 파일일 경우에만 출력
                System.out.println("File: " + entry.getFilename());
                if(entry.getFilename().contains(".jpg") || entry.getFilename().contains(".png") || entry.getFilename().contains(".jpeg")) {
                    list.add(entry.getFilename());
                }
            }
        }

        return list;
    }

    /**
     * SFTP 연결
     */
    public synchronized void connection() {
        init(Info.host, Info.userName, Info.password, Info.port, null);
    }

    public synchronized ChannelSftp init(String host, String userName, String password, int port, String privateKey, String key) {
        JSch jSch = new JSch();
        Channel channel = null;
        try {
            if(privateKey != null) {//개인키가 존재한다면
                jSch.addIdentity(privateKey);
            }
            session = jSch.getSession(userName, host, port);

            if(privateKey == null && password != null) {//개인키가 없다면 패스워드로 접속
                session.setPassword(password);
            }

            // 프로퍼티 설정
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no"); // 접속 시 hostkeychecking 여부
            session.setConfig(config);
            session.connect();

            //sftp로 접속
            channel = session.openChannel("sftp");
//            channel.connect();

            channelSftp = (ChannelSftp) channel;
            System.out.println("channelSftp: " + channelSftp.toString());
            channelSftp.connect();
        } catch (JSchException e) {
            System.out.println("init: " + e.getCause());
        }

        return channelSftp;
    }

    /**
     * 연결 종료
     */
    public void disconnection() {
        try {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        } catch (Exception e) {
            System.out.println("SFTP disconnection error: " + e.getMessage());
        } finally {
            channelSftp = null;
            session = null;
        }
    }
}

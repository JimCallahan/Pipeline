// PipelineRun.cpp : main project file.

#include "stdafx.h"

using namespace System;
using namespace System::Text;
using namespace System::IO;
using namespace System::Security::Cryptography;

void 
usage() 
{
  Console::WriteLine("usage: PipelineRun username cipher command [args...]\n");
  exit(EXIT_FAILURE);
}

int main(array<System::String ^> ^args)
{
  if(args->GetLength(0) < 3) 
    usage();
  
  /* user to impersonate */ 
  String^ username = (String^) args->GetValue(0);

  /* get decrypted password of user to impersonate */ 
  String^ password;
  {
    /* get encrypted bytes from Base64 argument string */ 
    array<unsigned char>^ encryptedBytes = 
      Convert::FromBase64String((System::String^) args->GetValue(1));

    /* create a crypography provider */ 
    RC2CryptoServiceProvider^ rc2Crypto = gcnew RC2CryptoServiceProvider();

    /* create a decryptor */
    ICryptoTransform^ decryptor;
    {
      /* 128-bit secret key */ 
      array<unsigned char>^ key = { 
	0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 
	0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F, 0x50 
      };
      
      /* 64-bit initialization vector */
      array<unsigned char>^ iv = {
	0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48
      };

      decryptor = rc2Crypto->CreateDecryptor(key, iv);
    }

    /* create a decrtyption stream */
    MemoryStream^ mstream = gcnew MemoryStream(encryptedBytes);
    CryptoStream^ cstream = gcnew CryptoStream(mstream, decryptor, CryptoStreamMode::Read);
    
    /* get the decrypted bytes from the stream */
    array<unsigned char>^ plainBytes = gcnew array<unsigned char>(encryptedBytes->Length);
    cstream->Read(plainBytes, 0, plainBytes->Length);

    /* convert raw bytes to plaintext */
    ASCIIEncoding^ textConverter = gcnew ASCIIEncoding();
    password = textConverter->GetString(plainBytes);
  }

  Console::WriteLine("User Name: {0}", username); 
  Console::WriteLine("Decrypted Password: {0}", password); 

  // command and arguments... 
  
  Console::WriteLine("\n");

  return EXIT_SUCCESS;
}

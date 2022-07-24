package com.example.sharedtaxitogether

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.example.sharedtaxitogether.databinding.FragmentProfileBinding
import com.example.sharedtaxitogether.dialog.EditNicknameDialog
import com.example.sharedtaxitogether.dialog.EditPasswordDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {
    lateinit var mainActivity: MainActivity
    private lateinit var binding: FragmentProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var room: AppDatabase

    //자동로그인
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        db = Firebase.firestore

        bind()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity)
        editor = sharedPreferences.edit()

        room = Room.databaseBuilder(
            mainActivity,
            AppDatabase::class.java, "UserDB"
        ).allowMainThreadQueries().build()

        initView()

        return binding.root
    }

    private fun initView() {
        Thread {
            activity?.runOnUiThread {
                when (room.userDao().getGender()) {
                    "Male" -> binding.genderImgView.setImageResource(R.drawable.male)
                    "Female" -> binding.genderImgView.setImageResource(R.drawable.female)
                }
                binding.nicknameTextView.text = room.userDao().getNickname()
                binding.scoreTextView.text = room.userDao().getScore()
                binding.emailTextView.text = room.userDao().getEmail()
                binding.passwdTextView.text = room.userDao().getPassword()
                binding.phoneTextView.text = room.userDao().getPhone()
            }
        }.start()
    }


    private fun bind() {
        binding.editNickname.setOnClickListener {
            val dialog = EditNicknameDialog(mainActivity)
            dialog.myDialog()

            dialog.setOnClickListener(object: EditNicknameDialog.OnDialogClickListener{
                override fun onClicked(nickname: String) {
                    binding.nicknameTextView.text = nickname
                    db.collection("users").document(room.userDao().getUid())
                        .update("nickname", nickname)
                        .addOnSuccessListener {
                            // TODO room 변경
                        }
                }
            })
        }
        binding.editEmail.setOnClickListener {
            // 중복확인, 유효성검사, db수정, ui 수정, 메일인증


        }
        binding.editPassword.setOnClickListener {
            val passwordDialog = EditPasswordDialog(mainActivity)
            passwordDialog.myDialog()

            passwordDialog.setOnClickListener(object: EditPasswordDialog.OnDialogClickListener{
                override fun onClicked(password: String) {
                    binding.passwdTextView.text = password
                    db.collection("users").document(room.userDao().getUid())
                        .update("password", password)
                        .addOnSuccessListener {
                            // TODO room 변경
                        }
                }
            })
        }
        binding.editPhone.setOnClickListener {

        }
        binding.editAccountAddress.setOnClickListener {

        }

        binding.logoutTextView.setOnClickListener {
            logout()
        }
        binding.withdrawTextView.setOnClickListener {
            withdraw()
        }
        binding.profileImgView.setOnClickListener {
            // TODO 프로필 변경
//            Log.d(TAG, "프로필 변경 버튼 클릭")
//            when {
//                ContextCompat.checkSelfPermission(
//                    mainActivity,
//                    android.Manifest.permission.READ_EXTERNAL_STORAGE
//                ) == PackageManager.PERMISSION_GRANTED
//                ->
//                    //권한 존재 이미지 가져오기
//                    getImageFomAlbum()
//            }
//            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) ->{
//            showPermissionContextPopup()
//        }

        }
    }

//    private fun getImageFomAlbum() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "image/*"
//        startActivityForResult(intent, 1)
//    }

    private fun withdraw() {
        val builder = AlertDialog.Builder(mainActivity)
        builder.setTitle("회원탈퇴")
            .setMessage("${room.userDao().getNickname()}님 정말 탈퇴하시겠습니까?")
            .setPositiveButton("탈퇴하기",
                DialogInterface.OnClickListener { _, _ ->
                    val token = room.userDao().getUid()
                    db.collection("users").document(token)
                        .delete()
                        .addOnSuccessListener {
                            Log.d(TAG, "회원탈퇴 성공")
                            startActivity(Intent(mainActivity, LoginActivity::class.java))
                            activity?.finish()
                        }
                })
            .setNegativeButton("취소",
                DialogInterface.OnClickListener { _, _ ->
                    Log.d(TAG, "회원탈퇴 취소")
                })
        builder.show()
    }

    private fun logout() {
        val user = room.userDao().getUser()
        Log.d(TAG, user.toString())
        room.userDao().delete(user)

        editor.putString("email", "")
        editor.putString("password", "")
        editor.commit()

        startActivity(Intent(mainActivity, LoginActivity::class.java))
        activity?.finish()
    }

    companion object {
        private const val TAG = "profileFragment"
    }
}
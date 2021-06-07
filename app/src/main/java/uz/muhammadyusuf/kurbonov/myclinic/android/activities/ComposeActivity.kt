package uz.muhammadyusuf.kurbonov.myclinic.android.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import uz.muhammadyusuf.kurbonov.myclinic.android.works.views.OverlayCompose
import uz.muhammadyusuf.kurbonov.myclinic.api.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.api.toContact
import uz.muhammadyusuf.kurbonov.myclinic.core.State
import uz.muhammadyusuf.kurbonov.myclinic.utils.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.utils.NetworkIOException
import java.io.IOException

class ComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val customer = GsonBuilder().create()
                .fromJson(customerJson(), CustomerDTO::class.java).toContact()
            setContent {
                Column {
                    var state by remember {
                        mutableStateOf<State>(State.None)
                    }
                    OverlayCompose(state = state)

                    @Suppress("ThrowableNotThrown") val states = arrayListOf(
                        State.Started,
                        State.Finished,
                        State.Found(customer, CallDirection.INCOME),
                        State.NoConnectionState,
                        State.NotFound,
                        State.None,
                        State.Searching,
                        State.TooSlowConnectionError,
                        State.AddNewCustomerRequest("+998913975538"),
                        State.AuthRequest("+998913975538"),
                        State.Error(NetworkIOException(IOException())),
                        State.PurposeRequest(customer, "1158498494984"),
                    )
                    LazyRow {
                        items(states) {
                            Button(onClick = { state = it }) {
                                Text(text = it.javaClass.simpleName)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun customerJson() = """
    {
  "total": 1,
  "limit": 50,
  "skip": 0,
  "data": [
    {
      "_id": "5eff112313789959bfeaf79f",
      "elementaryExam": {
        "bite": null,
        "OralHygieneIndex": null,
        "OralMucosa": "OralMucosa",
        "xRayAndLab": "xRayAndLab",
        "map": [
          {
            "tooth": 17,
            "diagnosis": "implant"
          },
          {
            "tooth": 36,
            "diagnosis": "missing"
          },
          {
            "tooth": 37,
            "diagnosis": "missing"
          },
          {
            "tooth": 38,
            "diagnosis": "missing"
          },
          {
            "tooth": 48,
            "diagnosis": "missing"
          },
          {
            "tooth": 28,
            "diagnosis": "missing"
          },
          {
            "tooth": 21,
            "diagnosis": "implant"
          },
          {
            "tooth": 44,
            "diagnosis": "implant"
          }
        ]
      },
      "userId": [
        "5dc2beab0af9c9e30a0ea0f5",
        "5dc3c93b9a76124f53746502",
        "5de0f2ef62d8bb50876d833b",
        "5dcf970025997c4d2789202e",
        "5dc3c9d89a76124f53746503"
      ],
      "tags": [
        "5e9c912501d0e9638d096591"
      ],
      "balance": 0,
      "avatar_id": "5eb033d791320698c7b3dd8c",
      "gender": "male",
      "allow_general_sms": true,
      "allow_promo_sms": true,
      "birth_date": "1995-01-12T00:00:00.000Z",
      "last_name": "Иванов",
      "first_name": "Иван",
      "phone": "+998(90) 350-04-90",
      "deleted": false,
      "companyId": "5dc2bf249a76124f5374648b",
      "createdAt": "2020-07-03T11:06:11.992Z",
      "updatedAt": "2021-03-21T09:16:59.718Z",
      "__v": 0,
      "customerIndex": "1334",
      "discountPercent": 100,
      "source": {},
      "stats": {
        "new": 5,
        "completed": 6,
        "confirmed": 8,
        "arrived": 4,
        "started": 2,
        "cancelled": 3,
        "notConfirmed": 7
      },
      "debtReturn": {
        "date": null,
        "text": null
      },
      "lastVisit": "2021-01-21T05:00:00.000Z",
      "avatar": {
        "_id": "5eb033d791320698c7b3dd8c",
        "previewSize": 5911,
        "tags": [
          "Images"
        ],
        "label": "i.jpeg",
        "filename": null,
        "size": 10196,
        "mimetype": "image/jpeg",
        "tooth": null,
        "category": "customers",
        "userId": "5dc2beab0af9c9e30a0ea0f5",
        "companyId": "5dc2bf249a76124f5374648b",
        "createdAt": "2020-05-04T15:25:11.380Z",
        "updatedAt": "2020-05-04T15:25:11.423Z",
        "__v": 0,
        "preview": null,
        "url": null
      },
      "schedule": [],
      "can_update": true,
      "blocked": false,
      "can_delete": true
    }
  ],
  "elementaryExam": {
    "map": []
  },
  "meta": [
    {
      "_id": "male",
      "count": 104
    },
    {
      "_id": "female",
      "count": 95
    },
    {
      "_id": null,
      "count": 25
    }
  ],
  "archived": 37,
  "prepaid": 22,
  "debtors": 18,
  "totalCustomers": 224,
  "appointments": [
    {
      "prev": {
        "_id": "6055d31cfc2a0c6fccafeb0c",
        "services": [
          {
            "treatmentId": "5dc3c84d9a76124f537464fe",
            "userId": "5dc3c9d89a76124f53746503",
            "startAt": "2021-03-20T06:30:00.000Z",
            "quantity": 1,
            "duration": 60,
            "workPlaceId": null
          }
        ],
        "notify": false,
        "startAt": "2021-03-20T06:30:00.000Z",
        "endAt": "2021-03-20T07:30:00.000Z",
        "customerId": "5eff112313789959bfeaf79f",
        "status": "notConfirmed",
        "userId": "5dc2beab0af9c9e30a0ea0f5",
        "companyId": "5dc2bf249a76124f5374648b",
        "createdAt": "2021-03-20T10:49:00.555Z",
        "updatedAt": "2021-03-20T10:49:00.555Z",
        "__v": 0
      }
    }
  ]
}
    """
}
